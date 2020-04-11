package com.kotori316.infchest.integration;

import java.math.BigInteger;
import java.util.List;

import javax.annotation.Nonnull;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import com.kotori316.infchest.tiles.TileInfChest;

import static com.kotori316.infchest.integration.Waila.Waila_ModId;

@net.minecraftforge.fml.common.Optional.Interface(modid = Waila_ModId, iface = "mcp.mobius.waila.api.IWailaDataProvider")
public class WailaProvider implements IWailaDataProvider {
    private static final String NBT_ITEM = "waila_item";
    private static final String NBT_COUNT = "waila_count";

    @Nonnull
    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = Waila_ModId)
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof TileInfChest) {
            NBTTagCompound data = accessor.getNBTData();
            ItemStack stack;
            if (data.hasKey(NBT_ITEM, Constants.NBT.TAG_COMPOUND))
                stack = new ItemStack(data.getCompoundTag(NBT_ITEM));
            else
                stack = new ItemStack(data.getCompoundTag(TileInfChest.NBT_ITEM));
            if (!stack.isEmpty()) {
                BigInteger integer;
                if (data.hasKey(NBT_COUNT, Constants.NBT.TAG_BYTE_ARRAY))
                    integer = new BigInteger(data.getByteArray(NBT_COUNT));
                else if (data.hasKey(TileInfChest.NBT_COUNT, Constants.NBT.TAG_STRING))
                    integer = new BigInteger(data.getString(TileInfChest.NBT_COUNT));
                else
                    integer = BigInteger.ZERO;
                tooltip.add(stack.getDisplayName());
                tooltip.add(integer.toString());
            }
        }
        return tooltip;
    }

    @Nonnull
    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = Waila_ModId)
    public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos) {
        if (te instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) te;
            tag.setTag(NBT_ITEM, chest.getStack(1).serializeNBT());
            tag.setByteArray(NBT_COUNT, chest.itemCount().toByteArray());
        }
        return tag;
    }
}
