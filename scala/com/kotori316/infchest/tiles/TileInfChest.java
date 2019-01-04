package com.kotori316.infchest.tiles;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.packets.ItemCountMessage;
import com.kotori316.infchest.packets.PacketHandler;

public class TileInfChest extends TileEntity implements HasInv, IRunUpdates {

    private ItemStack holding = ItemStack.EMPTY;
    private NonNullList<ItemStack> inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    private BigInteger count = BigInteger.ZERO;
    private String customName;
    public static final String NBT_ITEM = "item";
    public static final String NBT_COUNT = "count";
    private static final String NBT_CUSTOM_NAME = "custom_name";
    public static final String NBT_BLOCK_TAG = "BlockEntityTag";
    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
    private InfItemHandler itemHandler = new InfItemHandler(this);
    public static final Predicate<TileInfChest> IS_EMPTY = TileInfChest::isEmpty;
    private List<Runnable> updateRunnables = new ArrayList<>();

    public TileInfChest() {
        addUpdate(() -> PacketHandler.sendToPoint(new ItemCountMessage(this, this.itemCount())));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag(NBT_ITEM, holding.serializeNBT());
        compound.setString(NBT_COUNT, count.toString());
        Optional.ofNullable(customName).ifPresent(s -> compound.setString(NBT_CUSTOM_NAME, s));
        ItemStackHelper.saveAllItems(compound, inventory);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        holding = new ItemStack(compound.getCompoundTag(NBT_ITEM));
        if (compound.hasKey(NBT_COUNT)) {
            try {
                count = new BigInteger(compound.getString(NBT_COUNT));
            } catch (NumberFormatException e) {
                InfChest.LOGGER.error("TileInfChest loading problem.", e);
                count = BigInteger.ZERO;
            }
        } else {
            count = BigInteger.ZERO;
        }
        if (compound.hasKey(NBT_CUSTOM_NAME))
            customName = compound.getString(NBT_CUSTOM_NAME);
        ItemStackHelper.loadAllItems(compound, inventory);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return serializeNBT();
    }

    public NBTTagCompound getBlockTag() {
        NBTTagCompound nbtTagCompound = serializeNBT();
        Stream.of("x", "y", "z", "id").forEach(nbtTagCompound::removeTag);
        return nbtTagCompound;
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : InfChest.modID + ":tile." + BlockInfChest.name;
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    @Override
    public ITextComponent getDisplayName() {
        return hasCustomName() ? new TextComponentString(getName()) : null;
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
        ItemStack insert = getStackInSlot(0);
        if (isItemValidForSlot(0, insert)) {
            addStack(insert);
        }
        ItemStack out = getStackInSlot(1);
        // Make sure out item is equal to holding.
        boolean outFlag = out.isEmpty() || ItemStack.areItemsEqual(holding, out) && ItemStack.areItemStackTagsEqual(holding, out);
        if (outFlag && out.getCount() < holding.getMaxStackSize() && gt(count, 0)) {
            int sub = out.getMaxStackSize() - out.getCount();
            if (gt(count, sub)) { //count > sub
                ItemStack itemStack = copyAmount(holding, out.getMaxStackSize());
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
        int amount = INT_MAX.min(count).intValueExact();
        return copyAmount(holding, amount);
    }

    public BigInteger itemCount() {
        return count;
    }

    @SideOnly(Side.CLIENT)
    public void setCount(BigInteger count) {
        this.count = count;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return getWorld().getTileEntity(getPos()) == this && player.getDistanceSq(getPos()) <= 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        switch (index) {
            case 0:
                return holding.isEmpty() || (ItemStack.areItemsEqual(holding, stack) && ItemStack.areItemStackTagsEqual(holding, stack));
            case 1:
                return false;
            default:
                return false;
        }
    }

    @Override
    public void clear() {
        inventory.clear();
        holding = ItemStack.EMPTY;
        count = BigInteger.ZERO;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return super.getCapability(capability, facing);
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
        updateRunnables.add(runnable);
    }

    @Override
    public List<Runnable> getUpdates() {
        return updateRunnables;
    }
}
