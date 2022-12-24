package com.kotori316.infchest.common.blocks;

import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.integration.StorageBoxStack;
import com.kotori316.infchest.common.tiles.TileInfChest;

public abstract class BlockInfChest extends BaseEntityBlock {
    public static final String name = InfChest.modID;
    public final BlockItem itemBlock;

    public BlockInfChest() {
        super(Block.Properties.of(Material.METAL).strength(1.0f));
        itemBlock = new ItemInfChest(this);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return InfChest.accessor.INF_CHEST_TYPE().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult rayTrace) {
        if (!player.isCrouching()) {
            if (!worldIn.isClientSide) {
                if (StorageBoxStack.moveToStorage(worldIn, pos, player, hand)) return InteractionResult.SUCCESS;
                worldIn.getBlockEntity(pos, InfChest.accessor.INF_CHEST_TYPE()).ifPresent(t ->
                    this.openGui(((ServerPlayer) player), t, pos));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, worldIn, pos, player, hand, rayTrace);
    }

    protected abstract void openGui(ServerPlayer player, TileInfChest chest, BlockPos pos);

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.hasCustomHoverName()) {
            worldIn.getBlockEntity(pos, InfChest.accessor.INF_CHEST_TYPE())
                .ifPresent(chest -> chest.setCustomName(stack.getDisplayName()));
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
            .ifPresent(tag -> stack.addTagElement("BlockEntityTag", tag));
    }
}
