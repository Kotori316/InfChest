package com.kotori316.infchest.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.guis.GuiHandler;
import com.kotori316.infchest.tiles.TileInfChest;

public class BlockInfChest extends BlockContainer {
    public static final String name = InfChest.modID;
    public final ItemBlock itemBlock;

    public BlockInfChest() {
        super(Material.IRON);
        setRegistryName(InfChest.modID, name);
        setUnlocalizedName(InfChest.modID + "." + name);
        setHardness(1.0f);
        setCreativeTab(CreativeTabs.DECORATIONS);
        itemBlock = new ItemBlock(this);
        itemBlock.setRegistryName(InfChest.modID, name);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileInfChest();
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
        if (!playerIn.isSneaking()) {
            playerIn.openGui(InfChest.getInstance(), GuiHandler.CHEST_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }
}
