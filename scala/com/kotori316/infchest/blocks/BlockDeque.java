package com.kotori316.infchest.blocks;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileDeque;

public class BlockDeque extends BlockWithEntity {
    public static final String name = "deque";
    public final BlockItem itemBlock;

    public BlockDeque() {
        super(AbstractBlock.Settings.of(Material.METAL).strength(1.0f).allowsSpawning((state, world, pos, type) -> false));
        itemBlock = new BlockItem(this, new Item.Settings().group(ItemGroup.DECORATIONS));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return InfChest.Register.DEQUE_TYPE.instantiate(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.getBlockEntity(pos) instanceof TileDeque deque) {
            if (!world.isClient) {
                player.sendMessage(new LiteralText("Items: " + deque.size()), false);
                return ActionResult.CONSUME;
            } else {
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isOf(newState.getBlock())) {
            if (worldIn.getBlockEntity(pos) instanceof TileDeque deque) {
                deque.itemsList().forEach(stack -> ItemScatterer.spawn(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack));
            }
            super.onStateReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        super.appendTooltip(stack, world, tooltip, options);
        tooltip.add(new LiteralText("Use as First-In-First-Out Queue."));
        tooltip.add(new LiteralText("This block can hold 1 million items."));
    }

}
