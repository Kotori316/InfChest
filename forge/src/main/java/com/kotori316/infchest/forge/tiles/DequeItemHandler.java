package com.kotori316.infchest.forge.tiles;

import com.kotori316.infchest.common.tiles.TileDeque;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

record DequeItemHandler(TileDequeForge deque) implements IItemHandlerModifiable {

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
                deque.setChanged();
            }
            return ItemStack.EMPTY;
        }
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0) {
            ItemStack peek = deque.getInventory().isEmpty() ? null : deque.getInventory().get(0);
            if (peek == null) {
                return ItemStack.EMPTY;
            }
            if (peek.getCount() <= amount) {
                if (!simulate) {
                    deque.getInventory().remove(0);
                    deque.setChanged();
                }
                return peek.copy();
            } else {
                // split stack
                if (!simulate) {
                    ItemStack result = peek.split(amount);
                    deque.setChanged();
                    return result;
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
