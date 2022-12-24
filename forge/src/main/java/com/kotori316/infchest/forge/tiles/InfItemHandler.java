package com.kotori316.infchest.forge.tiles;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

record InfItemHandler(TileInfChestForge infChest) implements IItemHandlerModifiable {

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        infChest.setItem(slot, stack);
    }

    @Override
    public int getSlots() {
        return infChest.getContainerSize();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return infChest.getItem(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (isItemValid(0, stack)) {
            if (!simulate) {
                infChest.addStack(stack);
                infChest.setChanged();
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @NotNull
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
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return infChest.canPlaceItem(slot, stack);
    }
}
