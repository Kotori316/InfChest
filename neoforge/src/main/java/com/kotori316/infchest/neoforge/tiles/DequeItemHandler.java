package com.kotori316.infchest.neoforge.tiles;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import com.kotori316.infchest.common.tiles.TileDeque;

record DequeItemHandler(TileDequeNeoForge deque) implements IItemHandlerModifiable {

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        deque.setItem(slot, stack);
    }

    @Override
    public int getSlots() {
        return deque.getContainerSize();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return deque.getItem(slot);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (slot == 0 && getSlots() < TileDeque.MAX_COUNT) {
            if (!simulate) {
                deque.getInventory().add(stack.copy());
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @NotNull
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
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
