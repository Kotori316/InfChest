package com.kotori316.infchest.forge.tiles;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

/**
 * Has 2 slots, the first one must be always empty, and the second one has the actual item with stack size changed.
 *
 * @param infChest
 */
record InfItemHandler(TileInfChestForge infChest) implements IItemHandlerModifiable {

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        infChest.setItem(slot, stack);
    }

    @Override
    public int getSlots() {
        return 2;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot != 1) {
            return ItemStack.EMPTY;
        }
        var stack = infChest.getHolding();
        stack.setCount(Math.min(stack.getCount(), stack.getMaxStackSize()));
        return stack;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) return stack;
        if (!simulate) {
            infChest.addStack(stack);
            infChest.setChanged();
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot != 1) {
            // Only slot 1 allows extracting
            return ItemStack.EMPTY;
        }
        var item = infChest.getHolding();
        if (item.isEmpty()) {
            // Nothing to extract
            return ItemStack.EMPTY;
        }

        var extractCount = infChest.totalCount().min(BigInteger.valueOf(amount));
        if (!simulate) {
            infChest.decrStack(extractCount);
            infChest.setChanged();
        }
        // Safe to modify as item is already copied
        item.setCount(extractCount.intValueExact());
        return item;
    }

    @Override
    public int getSlotLimit(int slot) {
        return infChest.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (slot != 0) {
            // Only slot 0 allows inserting
            return false;
        }
        return infChest.canInsertFromOutside(stack);
    }
}
