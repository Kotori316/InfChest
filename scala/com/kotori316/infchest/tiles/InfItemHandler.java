package com.kotori316.infchest.tiles;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public class InfItemHandler implements IItemHandlerModifiable {

    private final TileInfChest infChest;

    InfItemHandler(TileInfChest infChest) {
        this.infChest = infChest;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        infChest.setInventorySlotContents(slot, stack);
    }

    @Override
    public int getSlots() {
        return infChest.getSizeInventory();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return infChest.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isItemValid(0, stack)) {
            if (!simulate) {
                infChest.addStack(stack);
                infChest.markDirty();
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 1) {
            if (!simulate) {
                ItemStack stack = infChest.decrStackSize(slot, amount);
                infChest.markDirty();
                return stack;
            } else {
                return infChest.getStack().split(amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return infChest.getInventoryStackLimit();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return infChest.isItemValidForSlot(slot, stack);
    }
}
