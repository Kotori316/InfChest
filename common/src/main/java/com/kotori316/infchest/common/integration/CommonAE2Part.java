package com.kotori316.infchest.common.integration;

import java.math.BigInteger;

import net.minecraft.world.item.ItemStack;

import com.kotori316.infchest.common.tiles.TileInfChest;

public final class CommonAE2Part {
    public static long insert(TileInfChest chest, long amount, ItemStack definition, boolean execute) {
        if (!chest.canPlaceItem(0, definition)) return 0; // The item is NOT acceptable.
        if (execute) {
            chest.addStack(definition, BigInteger.valueOf(amount));
            chest.setChanged();
        }
        return amount;
    }

    public static long extract(TileInfChest chest, long amount, ItemStack definition, boolean execute) {
        var holding = chest.getStack();
        var out = chest.getItem(1);
        if (ItemStack.isSameItemSameTags(definition, holding)) {
            BigInteger extractCount = BigInteger.valueOf(amount).min(chest.itemCount());
            if (execute) {
                // do subtract.
                chest.decrStack(extractCount);
                chest.setChanged();
            }
            if (extractCount.equals(chest.itemCount())) {
                // The caller requests more items than this chest holds.
                // Check the output slot and extract from it.
                if (ItemStack.isSameItemSameTags(definition, out)) {
                    var extraCount = BigInteger.valueOf(amount).subtract(chest.itemCount()).min(BigInteger.valueOf(out.getCount())).intValueExact();
                    if (execute) {
                        chest.removeItem(1, extraCount);
                        chest.setChanged();
                    }
                    return extractCount.longValue() + extraCount;
                } else {
                    // There is no extra item to be extracted.
                    return extractCount.longValue();
                }
            } else {
                // The demand is satisfied.
                return extractCount.longValue();
            }
        } else if (ItemStack.isSameItemSameTags(definition, out)) {
            int extractCount = (int) Math.min(out.getCount(), amount);
            if (execute) {
                chest.removeItem(1, extractCount);
                chest.setChanged();
            }
            return extractCount;
        } else {
            return 0; // This chest doesn't contain the item.
        }
    }
}
