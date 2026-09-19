package com.kotori316.infchest.common.blocks;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.integration.StorageBoxStack;
import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.common.tiles.TileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class BlockInfChest extends BaseEntityBlock {
    public static final String name = InfChest.modID;
    public final BlockItem itemBlock;

    public BlockInfChest() {
        var identifier = Identifier.fromNamespaceAndPath(InfChest.modID, name);
        super(Block.Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.IMMOVEABLE)
            .strength(1.0f)
            .isValidSpawn((_, _, _, _) -> false)
            .setId(ResourceKey.create(Registries.BLOCK, identifier))
        );
        itemBlock = new ItemInfChest(this, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier)).useBlockDescriptionPrefix());
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
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult rayTrace) {
        if (!player.isCrouching()) {
            if (!worldIn.isClientSide()) {
                if (StorageBoxStack.moveToStorage(worldIn, pos, player, hand)) return InteractionResult.SUCCESS;
                if (worldIn.getBlockEntity(pos) instanceof TileInfChest t) {
                    this.openGui(((ServerPlayer) player), t, pos);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, worldIn, pos, player, hand, rayTrace);
    }

    protected abstract void openGui(ServerPlayer player, TileInfChest chest, BlockPos pos);

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.has(DataComponents.CUSTOM_NAME)) {
            if (worldIn.getBlockEntity(pos) instanceof TileInfChest chest) {
                chest.setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        var pickBlock = super.getCloneItemStack(world, pos, state, includeData);
        saveChestNbtToStack(world.getBlockEntity(pos), pickBlock);
        saveCustomName(world.getBlockEntity(pos), pickBlock);
        return pickBlock;
    }

    public static void saveCustomName(@Nullable BlockEntity te, ItemStack drop) {
        Optional.ofNullable(te).filter(TileInfChest.class::isInstance).map(TileInfChest.class::cast)
            .filter(TileInfChest::hasCustomName)
            .map(TileInfChest::getName)
            .ifPresent(c -> drop.set(DataComponents.CUSTOM_NAME, c));
    }

    public static void saveChestNbtToStack(@Nullable BlockEntity entity, ItemStack stack) {
        Optional.ofNullable(entity)
            .filter(TileInfChest.class::isInstance)
            .map(TileInfChest.class::cast)
            .filter(Predicate.not(TileInfChest::isEmpty))
            .map(TileUtil::saveWithoutMetadata)
            .ifPresent(tag -> BlockItem.setBlockEntityData(stack, entity.getType(), tag));
    }

}
