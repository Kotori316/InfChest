package com.kotori316.infchest.tiles;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

class DequeItemHandler implements IItemHandlerModifiable {
    private final TileDeque deque;

    public DequeItemHandler(TileDeque deque) {
        this.deque = deque;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        deque.setInventorySlotContents(slot, stack);
    }

    @Override
    public int getSlots() {
        return deque.getSizeInventory();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return deque.getStackInSlot(slot);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot == 0 && getSlots() < TileDeque.MAX_COUNT) {
            if (!simulate) {
                deque.inventory.add(stack.copy());
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0) {
            ItemStack peek = deque.inventory.peek();
            if (peek == null) {
                return ItemStack.EMPTY;
            }
            if (peek.getCount() <= amount) {
                if (!simulate) {
                    deque.inventory.removeFirst();
                }
                return peek.copy();
            } else {
                // split stack
                if (!simulate) {
                    return peek.splitStack(amount);
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
        return deque.getInventoryStackLimit();
    }
}
