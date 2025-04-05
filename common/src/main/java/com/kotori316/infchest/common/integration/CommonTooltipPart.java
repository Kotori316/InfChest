package com.kotori316.infchest.common.integration;

import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.common.tiles.TileUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.math.BigInteger;
import java.util.List;

public final class CommonTooltipPart {
    private static final String NBT_KEY_ITEM = "tooltip_item";
    private static final String NBT_KEY_COUNT = "tooltip_count";

    public static List<Component> getTooltipBodyParts(CompoundTag data, BlockEntity entity) {
        ItemStack stack = ItemStack.parse(TileUtil.providerFromEntity(entity), data.getCompoundOrEmpty(NBT_KEY_ITEM)).orElse(ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            BigInteger integer = data.getByteArray(NBT_KEY_COUNT).map(BigInteger::new).orElse(BigInteger.ZERO);
            return List.of(stack.getDisplayName(), Component.literal(integer.toString()));
        } else {
            return List.of();
        }
    }

    public static void addTileData(CompoundTag destination, BlockEntity maybeChest) {
        if (maybeChest instanceof TileInfChest chest) {
            destination.put(NBT_KEY_ITEM, chest.getItem(1).save(TileUtil.providerFromEntity(chest)));
            destination.putByteArray(NBT_KEY_COUNT, chest.totalCount().toByteArray());
        }
    }
}
