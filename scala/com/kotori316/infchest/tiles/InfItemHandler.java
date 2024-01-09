package com.kotori316.infchest.tiles;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

record InfItemHandler(TileInfChest infChest) implements IItemHandler {

    @Override
    public int getSlots() {
        return 1;
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
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
        var holding = infChest.getHolding();
        return holding.isEmpty() || ItemHandlerHelper.canItemStacksStack(holding, stack);
    }
}
