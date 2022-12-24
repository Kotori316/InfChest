package com.kotori316.infchest.fabric.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;

public final class BlockInfChestFabric extends BlockInfChest {
    @Override
    protected void openGui(ServerPlayer player, TileInfChest chest, BlockPos pos) {
        player.openMenu(chest);
    }

}
