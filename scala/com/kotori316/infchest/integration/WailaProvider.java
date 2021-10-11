package com.kotori316.infchest.integration;
/*
import java.math.BigInteger;
import java.util.List;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.entity.player.ServerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.util.text.Component;
import net.minecraft.util.text.TextComponent;
import net.minecraft.world.Level;

import com.kotori316.infchest.tiles.TileInfChest;

public class WailaProvider implements IServerDataProvider<BlockEntity>, IComponentProvider {
    private static final String NBT_ITEM = "waila_item";
    private static final String NBT_COUNT = "waila_count";

    @Override
    public void appendBody(List<Component> tooltip, IDataAccessor accessor, IPluginConfig config) {
        BlockEntity t = accessor.getBlockEntity();
        if (t instanceof TileInfChest) {
            CompoundTag data = accessor.getServerData();
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
                    integer = new BigInteger(data.getString(TileInfChest.NBT_COUNT)).subtract(BigInteger.valueOf(stack.getMaxStackSize()));
                else
                    integer = BigInteger.ZERO;
                tooltip.add(stack.getDisplayName());
                tooltip.add(new TextComponent(integer.toString()));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer serverPlayer, Level world, BlockEntity te) {
        if (te instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) te;
            tag.put(NBT_ITEM, chest.getStack(1).serializeNBT());
            tag.putByteArray(NBT_COUNT, chest.itemCount().toByteArray());
        }
    }

}
*/