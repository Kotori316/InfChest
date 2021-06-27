package com.kotori316.infchest.tiles;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface HasInv extends net.minecraft.inventory.Inventory {

    @Override
    default int size() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return true;
    }

    @Override
    default ItemStack getStack(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeStack(int index, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeStack(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default void setStack(int index, ItemStack stack) {
    }

    @Override
    default boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    default boolean isValid(int index, ItemStack stack) {
        return true;
    }

    @Override
    default void clear() {
    }
}
