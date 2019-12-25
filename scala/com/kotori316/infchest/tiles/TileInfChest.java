package com.kotori316.infchest.tiles;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.INameable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.guis.ContainerInfChest;
import com.kotori316.infchest.packets.ItemCountMessage;
import com.kotori316.infchest.packets.PacketHandler;

public class TileInfChest extends TileEntity implements HasInv, IRunUpdates, INamedContainerProvider, INameable {

    private ItemStack holding = ItemStack.EMPTY;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    private BigInteger count = BigInteger.ZERO;
    private ITextComponent customName;
    public static final String NBT_ITEM = "item";
    public static final String NBT_COUNT = "count";
    private static final String NBT_CUSTOM_NAME = "custom_name";
    public static final String NBT_BLOCK_TAG = "BlockEntityTag";
    public static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private InfItemHandler handler = new InfItemHandler(this);
    private List<Runnable> updateRunnable = new ArrayList<>();

    public TileInfChest() {
        super(InfChest.Register.INF_CHEST_TYPE);
        addUpdate(() -> PacketHandler.sendToPoint(new ItemCountMessage(this, this.itemCount())));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.put(NBT_ITEM, holding.serializeNBT());
        compound.putString(NBT_COUNT, count.toString());
        Optional.ofNullable(customName).map(ITextComponent.Serializer::toJson).ifPresent(s -> compound.putString(NBT_CUSTOM_NAME, s));
        ItemStackHelper.saveAllItems(compound, inventory);
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        holding = ItemStack.read(compound.getCompound(NBT_ITEM));
        if (compound.contains(NBT_COUNT)) {
            try {
                count = new BigInteger(compound.getString(NBT_COUNT));
            } catch (NumberFormatException e) {
                InfChest.LOGGER.error("TileInfChest loading problem.", e);
                count = BigInteger.ZERO;
            }
        } else {
            count = BigInteger.ZERO;
        }
        if (compound.contains(NBT_CUSTOM_NAME))
            customName = ITextComponent.Serializer.fromJson(compound.getString(NBT_CUSTOM_NAME));
        ItemStackHelper.loadAllItems(compound, inventory);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return serializeNBT();
    }

    public CompoundNBT getBlockTag() {
        CompoundNBT nbtTagCompound = serializeNBT();
        Stream.of("x", "y", "z", "id", "ForgeCaps", "ForgeData").forEach(nbtTagCompound::remove);
        return nbtTagCompound;
    }

    @Override
    public ITextComponent getName() {
        return hasCustomName() ? customName : new TranslationTextComponent(InfChest.Register.CHEST.getTranslationKey());
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(ITextComponent name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? getCustomName() : getName();
    }

    @Override
    @Nullable
    public ITextComponent getCustomName() {
        return hasCustomName() ? customName : null;
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty) && holding.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        markDirty();
    }

    @Override
    public void markDirty() {
        super.markDirty();
        updateInv();
    }

    public void updateInv() {
        ItemStack insert = getStackInSlot(0);
        if (!insert.isEmpty()) {
            if (isItemValidForSlot(0, insert)) {
                addStack(insert);
            }
        }
        ItemStack out = getStackInSlot(1);
        // Make sure out item is equal to holding.
        boolean outFlag = out.isEmpty() || ItemStack.areItemsEqual(holding, out) && ItemStack.areItemStackTagsEqual(holding, out);
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
        if (!world.isRemote) {
            runUpdates();
        }
    }

    void addStack(ItemStack insert) {
        addStack(insert, BigInteger.valueOf(insert.getCount()));
    }

    public void addStack(ItemStack insert, BigInteger add) {
        count = count.add(add);
        if (holding.isEmpty())
            holding = copyAmount(insert, 1);
        inventory.set(0, ItemStack.EMPTY);
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

    public ItemStack getStack() {
        return getStack(INT_MAX.min(count).intValueExact());
    }

    public ItemStack getStack(int amount) {
        return copyAmount(holding, amount);
    }

    public BigInteger itemCount() {
        return count;
    }

    @OnlyIn(Dist.CLIENT)
    public void setCount(BigInteger count) {
        this.count = count;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return world.getTileEntity(getPos()) == this && player.getDistanceSq(getPos().getX(), getPos().getY(), getPos().getZ()) <= 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == 0) {
            return holding.isEmpty() || (ItemStack.areItemsEqual(holding, stack) && ItemStack.areItemStackTagsEqual(holding, stack));
        }
        return false;
    }

    @Override
    public void clear() {
        inventory.clear();
        holding = ItemStack.EMPTY;
        count = BigInteger.ZERO;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> handler));
        }
        return super.getCapability(cap, side);
    }

    private static ItemStack copyAmount(ItemStack stack, int amount) {
        ItemStack copy = stack.copy();
        copy.setCount(amount);
        return copy;
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
    public Container createMenu(int containerID, PlayerInventory inventory, PlayerEntity player) {
        return new ContainerInfChest(containerID, inventory, pos);
    }

}
