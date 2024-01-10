package com.kotori316.infchest.integration;

import com.kotori316.infchest.tiles.TileInfChest;
import mcp.mobius.waila.api.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.math.BigInteger;

public final class InfChestWthitProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {
    private static final String NBT_ITEM = "wthit_item";
    private static final String NBT_COUNT = "wthit_count";

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TileInfChest) {
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
                tooltip.addLine(stack.getDisplayName());
                tooltip.addLine(Component.literal(integer.toString()));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
        if (accessor.getTarget() instanceof TileInfChest chest) {
            tag.put(NBT_ITEM, chest.getHoldingWithOneCount().serializeNBT());
            tag.putByteArray(NBT_COUNT, chest.totalCount().toByteArray());
        }
    }
}
