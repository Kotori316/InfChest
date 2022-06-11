package com.kotori316.infchest.blocks;

import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class BlockInfChest extends BaseEntityBlock {
    public static final String name = InfChest.modID;
    public final BlockItem itemBlock;

    public BlockInfChest() {
        super(BlockBehaviour.Properties.of(Material.METAL).strength(1.0f).isValidSpawn((state, world, pos, type) -> false));
        itemBlock = new ItemInfChest(this);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return InfChest.Register.INF_CHEST_TYPE.create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos,
                              Player player, InteractionHand hand, BlockHitResult rayTrace) {
        if (!player.isShiftKeyDown() && worldIn.getBlockEntity(pos) instanceof TileInfChest chest) {
            if (!worldIn.isClientSide) {
                player.openMenu(chest);
                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, worldIn, pos, player, hand, rayTrace);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.hasCustomHoverName()) {
            if (worldIn.getBlockEntity(pos) instanceof TileInfChest chest) {
                chest.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        var pickBlock = super.getCloneItemStack(world, pos, state);
        saveChestNbtToStack(world.getBlockEntity(pos), pickBlock);
        saveCustomName(world.getBlockEntity(pos), pickBlock);
        return pickBlock;
    }

    public static void saveCustomName(@Nullable BlockEntity te, ItemStack drop) {
        Optional.ofNullable(te).filter(TileInfChest.class::isInstance).map(TileInfChest.class::cast)
            .filter(TileInfChest::hasCustomName)
            .map(TileInfChest::getName)
            .ifPresent(drop::setHoverName);
    }

    public static void saveChestNbtToStack(@Nullable BlockEntity entity, ItemStack stack) {
        Optional.ofNullable(entity)
            .filter(TileInfChest.class::isInstance)
            .map(TileInfChest.class::cast)
            .filter(Predicate.not(TileInfChest::isEmpty))
            .map(TileInfChest::saveWithoutMetadata)
            .ifPresent(tag -> stack.addTagElement(TileInfChest.NBT_BLOCK_TAG, tag));
    }
}
