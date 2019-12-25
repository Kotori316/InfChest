package com.kotori316.infchest.blocks;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileDeque;

public class BlockDeque extends ContainerBlock {
    public static final String name = "deque";
    public final BlockItem itemBlock;

    public BlockDeque() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.0f));
        setRegistryName(InfChest.modID, name);
        itemBlock = new BlockItem(this, new Item.Properties().group(ItemGroup.DECORATIONS));
        itemBlock.setRegistryName(InfChest.modID, name);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return InfChest.Register.DEQUE_TYPE.create();
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
    public boolean onBlockActivated(BlockState p_220051_1_, World p_220051_2_, BlockPos p_220051_3_, PlayerEntity p_220051_4_, Hand p_220051_5_, BlockRayTraceResult p_220051_6_) {
        return super.onBlockActivated(p_220051_1_, p_220051_2_, p_220051_3_, p_220051_4_, p_220051_5_, p_220051_6_);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof TileDeque) {
                TileDeque deque = (TileDeque) entity;
                deque.itemsList().forEach(stack -> InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack));
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(new StringTextComponent("Use as First-In-First-Out Queue."));
        tooltip.add(new StringTextComponent("This block can hold 1 million items."));
    }

}
