package com.kotori316.infchest.forge.blocks;

import com.kotori316.infchest.common.blocks.BlockDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockDequeForge extends BlockDeque {
    public BlockDequeForge() {
        super(BlockDequeForge::new);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

}
