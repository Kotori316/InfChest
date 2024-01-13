package com.kotori316.infchest.common.integration;

import java.math.BigInteger;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.kotori316.infchest.common.tiles.TileInfChest;

public final class CommonTooltipPart {
    private static final String NBT_KEY_ITEM = "tooltip_item";
    private static final String NBT_KEY_COUNT = "tooltip_count";

    public static List<Component> getTooltipBodyParts(CompoundTag data) {
        ItemStack stack;
        if (data.contains(NBT_KEY_ITEM)) {
            stack = ItemStack.of(data.getCompound(NBT_KEY_ITEM));
        } else {
            stack = ItemStack.of(data.getCompound(TileInfChest.NBT_ITEM));
        }
        if (!stack.isEmpty()) {
            BigInteger integer;
            if (data.contains(NBT_KEY_COUNT))
                integer = new BigInteger(data.getByteArray(NBT_KEY_COUNT));
            else if (data.contains(TileInfChest.NBT_COUNT))
                integer = new BigInteger(data.getString(TileInfChest.NBT_COUNT)).subtract(BigInteger.valueOf(stack.getMaxStackSize()));
            else
                integer = BigInteger.ZERO;
            return List.of(stack.getDisplayName(), Component.literal(integer.toString()));
        } else {
            return List.of();
        }
    }

    public static void addTileData(CompoundTag destination, BlockEntity maybeChest) {
        if (maybeChest instanceof TileInfChest chest) {
            destination.put(NBT_KEY_ITEM, chest.getItem(1).save(new CompoundTag()));
            destination.putByteArray(NBT_KEY_COUNT, chest.totalCount().toByteArray());
        }
    }
}
