package com.kotori316.infchest.tiles;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface HasInv extends net.minecraft.inventory.IInventory {

    @Override
    default void openInventory(PlayerEntity player) {
    }

    @Override
    default void closeInventory(PlayerEntity player) {
    }

    @Override
    default int getSizeInventory() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return true;
    }

    @Override
    default ItemStack getStackInSlot(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack decrStackSize(int index, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeStackFromSlot(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default void setInventorySlotContents(int index, ItemStack stack) {
    }

    @Override
    default int getInventoryStackLimit() {
        return 64;
    }

    @Override
    default boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    default boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    default void clear() {
    }
}
