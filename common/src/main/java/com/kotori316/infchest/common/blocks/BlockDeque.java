package com.kotori316.infchest.common.blocks;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileDeque;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.List;
import java.util.function.Supplier;

public class BlockDeque extends BaseEntityBlock {
    public static final String name = "deque";
    public final BlockItem itemBlock;
    protected final MapCodec<? extends BlockDeque> blockCodec;

    protected BlockDeque(Supplier<? extends BlockDeque> instanceSupplier) {
        super(Block.Properties.of().mapColor(MapColor.METAL).pushReaction(PushReaction.BLOCK).strength(1.0f));
        itemBlock = new BlockItem(this, new Item.Properties());
        this.blockCodec = simpleCodec(p -> instanceSupplier.get());
    }

    public BlockDeque() {
        this(BlockDeque::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return InfChest.accessor.DEQUE_TYPE().create(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            worldIn.getBlockEntity(pos, InfChest.accessor.DEQUE_TYPE())
                .map(TileDeque::itemsList)
                .ifPresent(l -> l.forEach(stack -> Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack)));
            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.literal("Use as First-In-First-Out Queue."));
        tooltip.add(Component.literal("This block can hold 1 million items."));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return this.blockCodec;
    }
}
