package com.kotori316.infchest.blocks;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class BlockInfChest extends ContainerBlock {
    public static final String name = InfChest.modID;
    public final BlockItem itemBlock;

    public BlockInfChest() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.0f));
        setRegistryName(InfChest.modID, name);
        itemBlock = new ItemInfChest(this);
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return InfChest.Register.INF_CHEST_TYPE.create();
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType func_225533_a_(BlockState state, World worldIn, BlockPos pos,
                                           PlayerEntity player, Hand hand, BlockRayTraceResult rayTrace) {
        if (!player.func_225608_bj_()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable(((TileInfChest) worldIn.getTileEntity(pos))).ifPresent(t ->
                    NetworkHooks.openGui(((ServerPlayerEntity) player), t, pos));
            }
            return ActionResultType.SUCCESS;
        }
        return super.func_225533_a_(state, worldIn, pos, player, hand, rayTrace);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.hasDisplayName()) {
            Optional.ofNullable(worldIn.getTileEntity(pos))
                .filter(TileInfChest.class::isInstance)
                .map(TileInfChest.class::cast)
                .ifPresent(chest -> chest.setCustomName(stack.getDisplayName()));
        }
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
        saveChestNbtToStack(world.getTileEntity(pos), pickBlock);
        saveCustomName(world.getTileEntity(pos), pickBlock);
        return pickBlock;
    }

    public static void saveCustomName(@Nullable TileEntity te, ItemStack drop) {
        Optional.ofNullable(te).filter(TileInfChest.class::isInstance).map(TileInfChest.class::cast)
            .filter(TileInfChest::hasCustomName)
            .map(TileInfChest::getName)
            .ifPresent(drop::setDisplayName);
    }

    public static void saveChestNbtToStack(@Nullable TileEntity entity, ItemStack stack) {
        Optional.ofNullable(entity)
            .filter(TileInfChest.class::isInstance)
            .map(TileInfChest.class::cast)
            .filter(InfChest.CHEST_NOT_EMPTY)
            .map(TileInfChest::getBlockTag)
            .ifPresent(tag -> stack.setTagInfo(TileInfChest.NBT_BLOCK_TAG, tag));
    }
}
