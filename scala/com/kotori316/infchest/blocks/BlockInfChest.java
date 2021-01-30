package com.kotori316.infchest.blocks;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.guis.GuiHandler;
import com.kotori316.infchest.integration.StorageBoxStack;
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
        itemBlock = new ItemInfChest(this);
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
            if (StorageBoxStack.isStorageBox(playerIn.getHeldItem(hand))) {
                if (!worldIn.isRemote) StorageBoxStack.moveToStorage(worldIn, pos, playerIn, hand);
                return true;
            }
            playerIn.openGui(InfChest.getInstance(), GuiHandler.CHEST_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (stack.hasDisplayName()) {
            Optional.ofNullable(worldIn.getTileEntity(pos))
                .filter(TileInfChest.class::isInstance)
                .map(TileInfChest.class::cast)
                .ifPresent(chest -> chest.setCustomName(stack.getDisplayName()));
        }
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
        saveChestNbtToStack(world.getTileEntity(pos), pickBlock);
        saveCustomName(world.getTileEntity(pos), pickBlock);
        return pickBlock;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {

        Optional.ofNullable(StatList.getBlockStats(this)).ifPresent(player::addStat);
        player.addExhaustion(0.005F);
        harvesters.set(player);
        if (!worldIn.isRemote && !worldIn.restoringBlockSnapshots) {
            // do not drop items while restoring blockStates, prevents item dupe
            int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
            NonNullList<ItemStack> drops = NonNullList.create();
            ItemStack chestStack = new ItemStack(this);
            saveChestNbtToStack(te, chestStack);
            saveCustomName(te, chestStack);
            drops.add(chestStack);
            float chance = ForgeEventFactory.fireBlockHarvesting(drops, worldIn, pos, state, fortune, 1.0f, false, harvesters.get());

            for (ItemStack drop : drops) {
                if (worldIn.rand.nextFloat() <= chance) {
                    spawnAsEntity(worldIn, pos, drop);
                }
            }
        }
        harvesters.set(null);
    }

    private static void saveCustomName(@Nullable TileEntity te, ItemStack drop) {
        Optional.ofNullable(te).filter(TileInfChest.class::isInstance).map(TileInfChest.class::cast)
            .filter(TileInfChest::hasCustomName)
            .map(TileInfChest::getName)
            .ifPresent(drop::setStackDisplayName);
    }

    private static void saveChestNbtToStack(@Nullable TileEntity entity, ItemStack stack) {
        Optional.ofNullable(entity)
            .filter(TileInfChest.class::isInstance)
            .map(TileInfChest.class::cast)
            .filter(InfChest.CHEST_NOT_EMPTY)
            .map(TileInfChest::getBlockTag)
            .ifPresent(tag -> stack.setTagInfo(TileInfChest.NBT_BLOCK_TAG, tag));
    }
}
