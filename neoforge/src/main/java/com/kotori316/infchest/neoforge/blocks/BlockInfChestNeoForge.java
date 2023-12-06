package com.kotori316.infchest.neoforge.blocks;

import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.NetworkHooks;

public final class BlockInfChestNeoForge extends BlockInfChest {
    public BlockInfChestNeoForge() {
        super(BlockInfChestNeoForge::new);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
        ItemStack pickBlock = super.getCloneItemStack(state, target, world, pos, player);
        saveChestNbtToStack(world.getBlockEntity(pos), pickBlock);
        saveCustomName(world.getBlockEntity(pos), pickBlock);
        return pickBlock;
    }

    @Override
    protected void openGui(ServerPlayer player, TileInfChest chest, BlockPos pos) {
        NetworkHooks.openScreen(player, chest, pos);
    }
}
