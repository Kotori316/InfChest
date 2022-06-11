package com.kotori316.infchest.tiles;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.guis.ContainerInfChest;
import com.kotori316.infchest.packets.ItemCountMessage;
import com.kotori316.infchest.packets.PacketHandler;

public class TileInfChest extends BlockEntity implements HasInv, IRunUpdates, ExtendedScreenHandlerFactory, Nameable {

    private ItemStack holding = ItemStack.EMPTY;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
    private BigInteger count = BigInteger.ZERO;
    private Component customName;
    public static final String NBT_ITEM = "item";
    public static final String NBT_COUNT = "count";
    private static final String NBT_CUSTOM_NAME = "custom_name";
    public static final String NBT_BLOCK_TAG = "BlockEntityTag";
    public static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private final List<Runnable> updateRunnable = new ArrayList<>();
    private final InsertingHook hook;

    public TileInfChest(BlockPos pos, BlockState state) {
        super(InfChest.Register.INF_CHEST_TYPE, pos, state);
        this.hook = InsertingHook.getInstance();
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        compound.putString(NBT_COUNT, count.toString());
        ContainerHelper.saveAllItems(compound, inventory);
        compound.put(NBT_ITEM, holding.save(new CompoundTag()));
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
        return this.saveWithoutMetadata();
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
        return ContainerHelper.removeItem(inventory, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(inventory, index);
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        inventory.set(index, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateInv();
    }

    private record MessageSender(ServerPlayer player, TileInfChest chest) implements Runnable {
        @Override
        public void run() {
            PacketHandler.sendToClientPlayer(new ItemCountMessage(chest, chest.itemCount()), player);
        }

        boolean playerEqual(Player player) {
            return this.player.getGameProfile().getId().equals(player.getGameProfile().getId());
        }
    }

    @Override
    public void startOpen(Player player) {
        HasInv.super.startOpen(player);
        if (level != null && !level.isClientSide && player instanceof ServerPlayer) {
            var messageSender = new MessageSender((ServerPlayer) player, this);
            this.updateRunnable.add(messageSender);
            messageSender.run();
        }
    }

    @Override
    public void stopOpen(Player player) {
        HasInv.super.stopOpen(player);
        if (level != null && !level.isClientSide && player instanceof ServerPlayer) {
            this.updateRunnable.removeIf(r -> (r instanceof MessageSender m) && m.playerEqual(player));
        }
    }

    public void updateInv() {
        ItemStack insert = getItem(0);
        if (!insert.isEmpty()) {
            if (canPlaceItem(0, insert)) {
                addStack(insert);
            }
        }
        ItemStack out = getItem(1);
        // Make sure out item is equal to holding.
        boolean outFlag = out.isEmpty() || stacksEqual(holding, out);
        if (outFlag && out.getCount() < holding.getMaxStackSize() && gt(count, 0)) {
            int sub = holding.getMaxStackSize() - out.getCount();
            if (gt(count, sub)) { //count > sub
                ItemStack itemStack = copyAmount(holding, holding.getMaxStackSize());
                count = count.subtract(BigInteger.valueOf(sub));
                inventory.set(1, itemStack); // Don't need to call markDirty() more.
            } else {
                // count <= sub
                ItemStack itemStack = copyAmount(holding, out.getCount() + count.intValueExact());
                count = BigInteger.ZERO;
                holding = ItemStack.EMPTY;
                inventory.set(1, itemStack); // Don't need to call markDirty() more.
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
                holding = copyAmount(insert, 1);
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
        updateInv();
    }

    public ItemStack getStackWithAmount() {
        return getStackWithAmount(INT_MAX.min(count).intValueExact());
    }

    public ItemStack getStackWithAmount(int amount) {
        return copyAmount(holding, amount);
    }

    public BigInteger itemCount() {
        return count;
    }

    @Environment(EnvType.CLIENT)
    public void setCount(BigInteger count) {
        this.count = count;
    }

    @Environment(EnvType.CLIENT)
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

    @Override
    public void clearContent() {
        inventory.clear();
        holding = ItemStack.EMPTY;
        count = BigInteger.ZERO;
    }

    private static ItemStack copyAmount(ItemStack stack, int amount) {
        ItemStack copy = stack.copy();
        copy.setCount(amount);
        return copy;
    }

    private static boolean stacksEqual(ItemStack s1, ItemStack s2) {
        return ItemStack.isSameIgnoreDurability(s1, s2) && ItemStack.tagMatches(s1, s2);
    }

    /**
     * @return true if bigInteger > i.
     */
    private static boolean gt(BigInteger bigInteger, int i) {
        return bigInteger.compareTo(BigInteger.valueOf(i)) > 0;
    }

    public static BigInteger countInInventory(CompoundTag chestNBT) {
        var inv = NonNullList.withSize(2, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(chestNBT, inv);
        return BigInteger.valueOf(inv.stream().mapToLong(ItemStack::getCount).sum());
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
        return new ContainerInfChest(containerID, inventory, worldPosition);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(worldPosition);
    }

}
