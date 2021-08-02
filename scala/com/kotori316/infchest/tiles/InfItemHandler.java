package com.kotori316.infchest.tiles;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

record InfItemHandler(TileInfChest infChest) implements IItemHandlerModifiable {

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        infChest.setItem(slot, stack);
    }

    @Override
    public int getSlots() {
        return infChest.getContainerSize();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return infChest.getItem(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (isItemValid(0, stack)) {
            if (!simulate) {
                infChest.addStack(stack);
                infChest.setChanged();
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
                ItemStack stack = infChest.removeItem(slot, amount);
                infChest.setChanged();
                return stack;
            } else {
                return infChest.getStack().split(amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return infChest.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return infChest.canPlaceItem(slot, stack);
    }
}
