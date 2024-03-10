package com.kotori316.infchest.neoforge.tiles;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

record InfItemHandler(TileInfChestNeoForge infChest) implements IItemHandlerModifiable {

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
        return infChest.canPlaceItem(slot, stack);
    }
}
