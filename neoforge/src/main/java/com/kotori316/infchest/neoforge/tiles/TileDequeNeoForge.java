package com.kotori316.infchest.neoforge.tiles;

import com.kotori316.infchest.common.tiles.TileDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;

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
    public IItemHandler getCapability(@Nullable Direction side) {
        return this.handler;
    }
}
