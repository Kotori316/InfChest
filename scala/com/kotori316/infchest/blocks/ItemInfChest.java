package com.kotori316.infchest.blocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

class ItemInfChest extends ItemBlock {

    private static final Predicate<ItemStack> STACK_EMPTY = ItemStack::isEmpty;
    private static final Predicate<String> STRING_EMPTY = String::isEmpty;

    ItemInfChest(Block block) {
        super(block);
        setRegistryName(InfChest.modID, BlockInfChest.name);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!world.setBlockState(pos, newState, 11)) return false;

        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.block) {
            if (world.getMinecraftServer() != null) {
                NBTTagCompound nbttagcompound = stack.getSubCompound(TileInfChest.NBT_BLOCK_TAG);
                TileEntity tileentity = world.getTileEntity(pos);
                if (nbttagcompound != null && tileentity != null) {
                    if (world.isRemote || !tileentity.onlyOpsCanSetNbt() || player.canUseCommandBlock()) {
                        NBTTagCompound tileNbt = tileentity.writeToNBT(new NBTTagCompound());
                        tileNbt.merge(nbttagcompound);
                        tileNbt.setInteger("x", pos.getX());
                        tileNbt.setInteger("y", pos.getY());
                        tileNbt.setInteger("z", pos.getZ());

                        tileentity.readFromNBT(tileNbt);
                        tileentity.markDirty();
                    }
                }
            }
            this.block.onBlockPlacedBy(world, pos, state, player, stack);

            if (player instanceof EntityPlayerMP)
                CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, stack);
        }

        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        NBTTagCompound n = stack.getSubCompound(TileInfChest.NBT_BLOCK_TAG);
        if (n != null) {
            Optional.of(new ItemStack(n.getCompoundTag(TileInfChest.NBT_ITEM))).filter(STACK_EMPTY.negate())
                    .map(ItemStack::getItem).map(Item::getRegistryName).map(ResourceLocation::toString).ifPresent(tooltip::add);
            Optional.of(n.getString(TileInfChest.NBT_COUNT)).filter(STRING_EMPTY.negate()).map(s -> s + " items").ifPresent(tooltip::add);
        }
    }
}
