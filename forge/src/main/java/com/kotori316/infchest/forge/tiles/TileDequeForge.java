package com.kotori316.infchest.forge.tiles;

import java.util.LinkedList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import com.kotori316.infchest.common.tiles.TileDeque;

public final class TileDequeForge extends TileDeque {
    private final IItemHandler handler;

    public TileDequeForge(BlockPos pos, BlockState state) {
        super(pos, state);
        handler = new DequeItemHandler(this);
    }

    LinkedList<ItemStack> getInventory() {
        return this.inventory;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
        }
        return super.getCapability(cap, side);
    }
}
