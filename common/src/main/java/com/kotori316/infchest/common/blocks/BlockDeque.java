package com.kotori316.infchest.common.blocks;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileDeque;

public class BlockDeque extends BaseEntityBlock {
    public static final String name = "deque";
    public final BlockItem itemBlock;

    public BlockDeque() {
        super(Block.Properties.of(Material.METAL).strength(1.0f));
        itemBlock = new BlockItem(this, new Item.Properties());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return InfChest.accessor.DEQUE_TYPE().create(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            worldIn.getBlockEntity(pos, InfChest.accessor.DEQUE_TYPE())
                .map(TileDeque::itemsList)
                .ifPresent(l -> l.forEach(stack -> Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack)));
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.literal("Use as First-In-First-Out Queue."));
        tooltip.add(Component.literal("This block can hold 1 million items."));
    }

}
