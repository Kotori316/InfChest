package com.kotori316.infchest.blocks;

import java.util.Optional;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class BlockInfChest extends BlockWithEntity {
    public static final String name = InfChest.modID;
    public final BlockItem itemBlock;

    public BlockInfChest() {
        super(AbstractBlock.Settings.of(Material.METAL).strength(1.0f).allowsSpawning((state, world, pos, type) -> false));
        itemBlock = new ItemInfChest(this);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return InfChest.Register.INF_CHEST_TYPE.instantiate(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World worldIn, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult rayTrace) {
        if (!player.isSneaking() && worldIn.getBlockEntity(pos) instanceof TileInfChest chest) {
            if (!worldIn.isClient) {
                player.openHandledScreen(chest);
                return ActionResult.CONSUME;
            } else {
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, worldIn, pos, player, hand, rayTrace);
    }

    @Override
    public void onPlaced(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onPlaced(worldIn, pos, state, placer, stack);
        if (stack.hasCustomName()) {
            if (worldIn.getBlockEntity(pos) instanceof TileInfChest chest) {
                chest.setCustomName(stack.getName());
            }
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        var pickBlock = super.getPickStack(world, pos, state);
        saveChestNbtToStack(world.getBlockEntity(pos), pickBlock);
        saveCustomName(world.getBlockEntity(pos), pickBlock);
        return pickBlock;
    }

    public static void saveCustomName(@Nullable BlockEntity te, ItemStack drop) {
        Optional.ofNullable(te).filter(TileInfChest.class::isInstance).map(TileInfChest.class::cast)
            .filter(TileInfChest::hasCustomName)
            .map(TileInfChest::getName)
            .ifPresent(drop::setCustomName);
    }

    public static void saveChestNbtToStack(@Nullable BlockEntity entity, ItemStack stack) {
        Optional.ofNullable(entity)
            .filter(TileInfChest.class::isInstance)
            .map(TileInfChest.class::cast)
            .filter(InfChest.CHEST_NOT_EMPTY)
            .map(TileInfChest::getBlockTag)
            .ifPresent(tag -> stack.putSubTag(TileInfChest.NBT_BLOCK_TAG, tag));
    }
}
