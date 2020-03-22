package com.kotori316.infchest.integration;

import java.math.BigInteger;
import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import com.kotori316.infchest.tiles.TileInfChest;

public class WailaProvider implements IServerDataProvider<TileEntity>, IComponentProvider {
    private static final String NBT_ITEM = "waila_item";
    private static final String NBT_COUNT = "waila_count";

    @Override
    public void appendBody(List<ITextComponent> tooltip, IDataAccessor accessor, IPluginConfig config) {
        TileEntity t = accessor.getTileEntity();
        if (t instanceof TileInfChest) {
            CompoundNBT data = accessor.getServerData();
            ItemStack stack;
            if (data.contains(NBT_ITEM)) {
                stack = ItemStack.read(data.getCompound(NBT_ITEM));
            } else {
                stack = ItemStack.read(data.getCompound(TileInfChest.NBT_ITEM));
            }
            if (!stack.isEmpty()) {
                BigInteger integer;
                if (data.contains(NBT_COUNT))
                    integer = new BigInteger(data.getByteArray(NBT_COUNT));
                else if (data.contains(TileInfChest.NBT_COUNT))
                    integer = new BigInteger(data.getString(TileInfChest.NBT_COUNT));
                else
                    integer = BigInteger.ZERO;
                tooltip.add(stack.getDisplayName());
                tooltip.add(new StringTextComponent(integer.toString()));
            }
        }
    }

    @Override
    public void appendServerData(CompoundNBT tag, ServerPlayerEntity serverPlayerEntity, World world, TileEntity te) {
        if (te instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) te;
            tag.put(NBT_ITEM, chest.getStack(1).serializeNBT());
            tag.putByteArray(NBT_COUNT, chest.itemCount().toByteArray());
        }
    }

}
