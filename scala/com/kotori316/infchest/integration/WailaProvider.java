package com.kotori316.infchest.integration;

import java.math.BigInteger;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.kotori316.infchest.tiles.TileInfChest;

public class WailaProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {
    private static final String NBT_ITEM = "waila_item";
    private static final String NBT_COUNT = "waila_count";

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity t = accessor.getBlockEntity();
        if (t instanceof TileInfChest) {
            CompoundTag data = accessor.getServerData();
            ItemStack stack;
            if (data.contains(NBT_ITEM)) {
                stack = ItemStack.of(data.getCompound(NBT_ITEM));
            } else {
                stack = ItemStack.of(data.getCompound(TileInfChest.NBT_ITEM));
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
        if (te instanceof TileInfChest chest) {
            tag.put(NBT_ITEM, chest.getStack(1).serializeNBT());
            tag.putByteArray(NBT_COUNT, chest.itemCount().toByteArray());
        }
    }

}
