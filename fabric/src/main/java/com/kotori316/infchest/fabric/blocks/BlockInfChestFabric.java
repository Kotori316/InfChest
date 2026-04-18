package com.kotori316.infchest.fabric.blocks;

import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class BlockInfChestFabric extends BlockInfChest {
    public BlockInfChestFabric() {
        super(BlockInfChestFabric::new);
    }

    @Override
    protected void openGui(ServerPlayer player, TileInfChest chest, BlockPos pos) {
        player.openMenu(chest);
    }

}
