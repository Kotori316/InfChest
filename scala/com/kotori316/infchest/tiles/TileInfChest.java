package com.kotori316.infchest.tiles;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.guis.ContainerInfChest;
import com.kotori316.infchest.packets.ItemCountMessage;
import com.kotori316.infchest.packets.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TileInfChest extends BlockEntity implements HasInv, IRunUpdates, MenuProvider, Nameable {

    private ItemStack holding = ItemStack.EMPTY;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    private BigInteger count = BigInteger.ZERO;
    private Component customName;
    public static final String NBT_ITEM = "item";
    public static final String NBT_COUNT = "count";
    private static final String NBT_CUSTOM_NAME = "custom_name";
    public static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private final InfItemHandler handler = new InfItemHandler(this);
    private final List<Runnable> updateRunnable = new ArrayList<>();
    private final InsertingHook hook;

    public TileInfChest(BlockPos pos, BlockState state) {
        super(InfChest.Register.INF_CHEST_TYPE, pos, state);
        addUpdate(() -> PacketHandler.sendToPoint(new ItemCountMessage(this, totalCount())));
        this.hook = InsertingHook.getInstance();
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        compound.putString(NBT_COUNT, count.toString());
        ContainerHelper.saveAllItems(compound, inventory);
        compound.put(NBT_ITEM, holding.serializeNBT());
        Optional.ofNullable(customName).map(Component.Serializer::toJson).ifPresent(s -> compound.putString(NBT_CUSTOM_NAME, s));
        super.saveAdditional(compound);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        holding = ItemStack.of(compound.getCompound(NBT_ITEM));
        if (compound.contains(NBT_COUNT)) {
            try {
                count = new BigDecimal(compound.getString(NBT_COUNT)).toBigIntegerExact();
            } catch (NumberFormatException | ArithmeticException e) {
                InfChest.LOGGER.error("TileInfChest loading problem.", e);
                count = BigInteger.ZERO;
            }
        } else {
            count = BigInteger.ZERO;
        }
        if (compound.contains(NBT_CUSTOM_NAME))
            customName = Component.Serializer.fromJson(compound.getString(NBT_CUSTOM_NAME));
        ContainerHelper.loadAllItems(compound, inventory);
        updateInv();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithFullMetadata();
    }

    @Override
    public Component getName() {
        return hasCustomName() ? customName : Component.translatable(InfChest.Register.CHEST.getDescriptionId());
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(Component name) {
        this.customName = name;
    }

    @Override
    public Component getDisplayName() {
        return hasCustomName() ? getCustomName() : getName();
    }

    @Override
    @Nullable
    public Component getCustomName() {
        return hasCustomName() ? customName : null;
    }

    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty) && holding.isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        var s = ContainerHelper.removeItem(inventory, index, count);
        if (index == 1) {
            if (level != null && !level.isClientSide) {
                decrStack(BigInteger.valueOf(s.getCount()));
            }
            setChanged();
        }
        return s;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        var s = ContainerHelper.takeItem(inventory, index);
        if (index == 1) {
            if (level != null && !level.isClientSide) {
                decrStack(BigInteger.valueOf(s.getCount()));
            }
            setChanged();
        }
        return s;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        var before = inventory.set(index, stack);
        if (index == 1) {
            if (level != null && !level.isClientSide) {
                if (!stack.isEmpty() && stacksEqual(holding, stack)) {
                    count = count.add(BigInteger.valueOf(stack.getCount()));
                }
                decrStack(BigInteger.valueOf(before.getCount()));
            }
        }
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateInv();
    }

    @Override
    public void startOpen(Player player) {
        if (level != null && !level.isClientSide) {
            runUpdates();
        }
    }

    public void updateInv() {
        ItemStack insert = getItem(0);
        if (!insert.isEmpty()) {
            if (canPlaceItem(0, insert)) {
                addStack(insert);
            }
        }
        // Set stack in output slot
        if (holding.isEmpty()) {
            // Chest has no items. Set empty stack in output slot
            inventory.set(1, ItemStack.EMPTY);
        } else {
            ItemStack out = getItem(1);
            if (out.isEmpty() || stacksEqual(holding, out)) {
                int expectedCount = totalCount().min(BigInteger.valueOf(holding.getMaxStackSize())).intValueExact();
                inventory.set(1, ItemHandlerHelper.copyStackWithSize(holding, expectedCount));
            }
        }

        if (level != null && !level.isClientSide) {
            runUpdates();
        }
    }

    void addStack(ItemStack insert) {
        addStack(insert, BigInteger.valueOf(insert.getCount()));
    }

    public void addStack(ItemStack insert, BigInteger add) {
        Optional<InsertingHook.Hook> hookObject = hook.findHookObject(insert);
        if (hookObject.isPresent()) {
            hookObject.ifPresent(h -> {
                // holding must not be empty.
                count = count.add(h.getCount(insert));
                inventory.set(0, h.removeAllItems(insert));
            });
        } else {
            count = count.add(add);
            if (holding.isEmpty())
                holding = ItemHandlerHelper.copyStackWithSize(insert, 1);
            inventory.set(0, ItemStack.EMPTY);
        }
    }

    /**
     * @param subs must be less than getCount().
     * @throws IllegalArgumentException if subs > count.
     */
    public void decrStack(BigInteger subs) {
        if (subs.compareTo(count) > 0) {
            // subs > count
            throw new IllegalArgumentException("subs > count");
        }
        count = count.subtract(subs);
        if (count.equals(BigInteger.ZERO)) {
            holding = ItemStack.EMPTY;
        }
    }

    public ItemStack getHolding() {
        if (!holding.isEmpty()) {
            return ItemHandlerHelper.copyStackWithSize(holding, INT_MAX.min(totalCount()).intValueExact());
        }
        ItemStack out = getItem(1);
        if (!out.isEmpty()) {
            return out.copy();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getHoldingWithOneCount() {
        if (!holding.isEmpty()) {
            return ItemHandlerHelper.copyStackWithSize(holding, 1);
        }
        ItemStack out = getItem(1);
        if (!out.isEmpty()) {
            return ItemHandlerHelper.copyStackWithSize(out, 1);
        }
        return ItemStack.EMPTY;
    }

    public BigInteger totalCount() {
        return count;
    }

    @OnlyIn(Dist.CLIENT)
    public void setCount(BigInteger count) {
        this.count = count;
    }

    @OnlyIn(Dist.CLIENT)
    public void setHolding(ItemStack holding) {
        this.holding = holding;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(getBlockPos()) == this && player.distanceToSqr(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()) <= 64;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == 0) {
            Optional<InsertingHook.Hook> hookObject = hook.findHookObject(stack);
            ItemStack secondStack = getItem(1);
            return (holding.isEmpty() && hookObject.isEmpty() && (secondStack.isEmpty() || stacksEqual(secondStack, stack)))
                || stacksEqual(holding, stack)
                || hookObject.filter(h -> h.checkItemAcceptable(holding, stack)).isPresent();
        }
        return false;
    }

    public boolean canInsertFromOutside(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!getItem(0).isEmpty()) return false; // To the disappearance of item in slot 0
        if (holding.isEmpty()) return true;
        return ItemHandlerHelper.canItemStacksStack(holding, stack);
    }

    @Override
    public void clearContent() {
        inventory.clear();
        holding = ItemStack.EMPTY;
        count = BigInteger.ZERO;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
        }
        return super.getCapability(cap, side);
    }

    private static boolean stacksEqual(ItemStack s1, ItemStack s2) {
        return ItemStack.isSame(s1, s2) && ItemStack.tagMatches(s1, s2);
    }

    /**
     * @return true if bigInteger > i.
     */
    private static boolean gt(BigInteger bigInteger, int i) {
        return bigInteger.compareTo(BigInteger.valueOf(i)) > 0;
    }

    @Override
    public void addUpdate(Runnable runnable) {
        updateRunnable.add(runnable);
    }

    @Override
    public List<Runnable> getUpdates() {
        return updateRunnable;
    }

    public static final String GUI_ID = InfChest.modID + ":gui_" + BlockInfChest.name;

    @Override
    public AbstractContainerMenu createMenu(int containerID, Inventory inventory, Player player) {
        return new ContainerInfChest(containerID, inventory, getBlockPos());
    }

}
