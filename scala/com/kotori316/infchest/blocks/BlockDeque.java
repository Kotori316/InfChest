package com.kotori316.infchest.blocks;

import java.util.List;

import javax.annotation.Nullable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileDeque;

public class BlockDeque extends BlockContainer {
    public static final String name = "deque";
    public final ItemBlock itemBlock;

    public BlockDeque() {
        super(Material.IRON);
        setRegistryName(InfChest.modID, name);
        setUnlocalizedName(InfChest.modID + "." + name);
        setHardness(1.0f);
        setCreativeTab(CreativeTabs.DECORATIONS);
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(InfChest.modID, name);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileDeque();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof TileDeque) {
            TileDeque deque = (TileDeque) entity;
            deque.itemsList().forEach(stack -> InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack));
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add("Use as First-In-First-Out Queue.");
        tooltip.add("This block can hold 1 million items.");
    }
}
