package com.kotori316.infchest.common.tiles;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface HasInv extends Container {
    @Override
    default void startOpen(ContainerUser user) {
    }

    @Override
    default void stopOpen(ContainerUser user) {
    }

    @Override
    default int getContainerSize() {
        return 1;
    }

    @Override
    default boolean isEmpty() {
        return true;
    }

    @Override
    default ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItem(int index, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    default ItemStack removeItemNoUpdate(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    default void setItem(int index, ItemStack stack) {
    }

    @Override
    default boolean stillValid(Player player) {
        return true;
    }

    @Override
    default boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    @Override
    default void clearContent() {
    }
}
