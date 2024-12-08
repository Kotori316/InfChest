package com.kotori316.infchest.neoforge.blocks;

import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockInfChestNeoForge extends BlockInfChest {
    public BlockInfChestNeoForge() {
        super(BlockInfChestNeoForge::new);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData, Player player) {
        ItemStack pickBlock = new ItemStack(this);
        saveChestNbtToStack(level.getBlockEntity(pos), pickBlock);
        saveCustomName(level.getBlockEntity(pos), pickBlock);
        return pickBlock;
    }

    @Override
    protected void openGui(ServerPlayer player, TileInfChest chest, BlockPos pos) {
        player.openMenu(chest, pos);
    }
}
