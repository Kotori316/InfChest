package com.kotori316.infchest.neoforge.tiles;

import java.util.LinkedList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.capabilities.Capabilities;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.common.tiles.TileDeque;

public final class TileDequeNeoForge extends TileDeque {
    private final IItemHandler handler;

    public TileDequeNeoForge(BlockPos pos, BlockState state) {
        super(pos, state);
        handler = new DequeItemHandler(this);
    }

    LinkedList<ItemStack> getInventory() {
        return this.inventory;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.ITEM_HANDLER) {
            return Capabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
        }
        return super.getCapability(cap, side);
    }
}
