package com.kotori316.infchest.tiles;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.guis.ContainerInfChest;

public class TileInfChest extends BlockEntity implements HasInv, IRunUpdates, ExtendedScreenHandlerFactory, Nameable, BlockEntityClientSerializable {

    private ItemStack holding = ItemStack.EMPTY;
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(size(), ItemStack.EMPTY);
    private BigInteger count = BigInteger.ZERO;
    private Text customName;
    public static final String NBT_ITEM = "item";
    public static final String NBT_COUNT = "count";
    private static final String NBT_CUSTOM_NAME = "custom_name";
    public static final String NBT_BLOCK_TAG = "BlockEntityTag";
    public static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private final List<Runnable> updateRunnable = new ArrayList<>();
    private final InsertingHook hook;

    public TileInfChest(BlockPos pos, BlockState state) {
        super(InfChest.Register.INF_CHEST_TYPE, pos, state);
        addUpdate(this::sync);
        this.hook = InsertingHook.getInstance();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        compound.putString(NBT_COUNT, count.toString());
        Inventories.writeNbt(compound, inventory);
        compound.put(NBT_ITEM, holding.writeNbt(new NbtCompound()));
        Optional.ofNullable(customName).map(Text.Serializer::toJson).ifPresent(s -> compound.putString(NBT_CUSTOM_NAME, s));
        return super.writeNbt(compound);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        holding = ItemStack.fromNbt(compound.getCompound(NBT_ITEM));
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
            customName = Text.Serializer.fromJson(compound.getString(NBT_CUSTOM_NAME));
        Inventories.readNbt(compound, inventory);
        updateInv();
    }

    public NbtCompound getBlockTag() {
        NbtCompound nbtTagCompound = writeNbt(new NbtCompound());
        Stream.of("x", "y", "z", "id", "ForgeCaps", "ForgeData").forEach(nbtTagCompound::remove);
        return nbtTagCompound;
    }

    @Override
    public Text getName() {
        return hasCustomName() ? customName : new TranslatableText(InfChest.Register.CHEST.getTranslationKey());
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(Text name) {
        this.customName = name;
    }

    @Override
    public Text getDisplayName() {
        return hasCustomName() ? getCustomName() : getName();
    }

    @Override
    @Nullable
    public Text getCustomName() {
        return hasCustomName() ? customName : null;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty) && holding.isEmpty();
    }

    @Override
    public ItemStack getStack(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        return Inventories.splitStack(inventory, index, count);
    }

    @Override
    public ItemStack removeStack(int index) {
        return Inventories.removeStack(inventory, index);
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        inventory.set(index, stack);
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        updateInv();
    }

    @Override
    public void onOpen(PlayerEntity player) {
        HasInv.super.onOpen(player);
        if (world != null && !world.isClient) {
            sync();
        }
    }

    public void updateInv() {
        ItemStack insert = getStack(0);
        if (!insert.isEmpty()) {
            if (isValid(0, insert)) {
                addStack(insert);
            }
        }
        ItemStack out = getStack(1);
        // Make sure out item is equal to holding.
        boolean outFlag = out.isEmpty() || stacksEqual(holding, out);
        if (outFlag && out.getCount() < holding.getMaxCount() && gt(count, 0)) {
            int sub = holding.getMaxCount() - out.getCount();
            if (gt(count, sub)) { //count > sub
                ItemStack itemStack = copyAmount(holding, holding.getMaxCount());
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
        if (world != null && !world.isClient) {
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
    public boolean canPlayerUse(PlayerEntity player) {
        return world != null && world.getBlockEntity(getPos()) == this && player.squaredDistanceTo(getPos().getX(), getPos().getY(), getPos().getZ()) <= 64;
    }

    @Override
    public boolean isValid(int index, ItemStack stack) {
        if (index == 0) {
            Optional<InsertingHook.Hook> hookObject = hook.findHookObject(stack);
            ItemStack secondStack = getStack(1);
            return (holding.isEmpty() && hookObject.isEmpty() && (secondStack.isEmpty() || stacksEqual(secondStack, stack)))
                || stacksEqual(holding, stack)
                || hookObject.filter(h -> h.checkItemAcceptable(holding, stack)).isPresent();
        }
        return false;
    }

    @Override
    public void clear() {
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
        return ItemStack.areItemsEqual(s1, s2) && ItemStack.areNbtEqual(s1, s2);
    }

    /**
     * @return true if bigInteger > i.
     */
    private static boolean gt(BigInteger bigInteger, int i) {
        return bigInteger.compareTo(BigInteger.valueOf(i)) > 0;
    }

    public static BigInteger countInInventory(NbtCompound chestNBT) {
        var inv = DefaultedList.ofSize(2, ItemStack.EMPTY);
        Inventories.readNbt(chestNBT, inv);
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
    public ScreenHandler createMenu(int containerID, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerInfChest(containerID, inventory, pos);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }
}
