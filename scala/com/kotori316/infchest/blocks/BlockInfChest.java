package com.kotori316.infchest.blocks;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class BlockInfChest extends BlockContainer {
    public static final String name = InfChest.modID;
    public final ItemBlock itemBlock;

    public BlockInfChest() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(1.0f));
        setRegistryName(InfChest.modID, name);
        itemBlock = new ItemInfChest(this);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return InfChest.INF_CHEST_TYPE.create();
    }

    @Override
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IWorldReaderBase world, BlockPos pos,
                                    EntitySpawnPlacementRegistry.SpawnPlacementType type, @Nullable EntityType<? extends EntityLiving> entityType) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable(((TileInfChest) worldIn.getTileEntity(pos))).ifPresent(t ->
                    NetworkHooks.openGui(((EntityPlayerMP) player), t, pos));
            }
            return true;
        }
        return super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ);
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
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player) {
        ItemStack pickBlock = super.getPickBlock(state, target, world, pos, player);
        saveChestNbtToStack(world.getTileEntity(pos), pickBlock);
        saveCustomName(world.getTileEntity(pos), pickBlock);
        return pickBlock;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {

        player.addStat(StatList.BLOCK_MINED.get(this));
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
            .ifPresent(drop::setDisplayName);
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
