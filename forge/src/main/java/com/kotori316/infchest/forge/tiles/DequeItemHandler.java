package com.kotori316.infchest.forge.tiles;

import javax.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.kotori316.infchest.common.tiles.TileDeque;

record DequeItemHandler(TileDequeForge deque) implements IItemHandlerModifiable {

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        deque.setItem(slot, stack);
    }

    @Override
    public int getSlots() {
        return deque.getContainerSize();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return deque.getItem(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot == 0 && getSlots() < TileDeque.MAX_COUNT) {
            if (!simulate) {
                deque.getInventory().add(stack.copy());
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0) {
            ItemStack peek = deque.getInventory().peek();
            if (peek == null) {
                return ItemStack.EMPTY;
            }
            if (peek.getCount() <= amount) {
                if (!simulate) {
                    deque.getInventory().removeFirst();
                }
                return peek.copy();
            } else {
                // split stack
                if (!simulate) {
                    return peek.split(amount);
                } else {
                    ItemStack t = peek.copy();
                    t.setCount(amount);
                    return t;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return deque.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return true;
    }
}
