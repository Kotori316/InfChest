package com.kotori316.infchest.common.integration;

import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.world.item.ItemStack;

import java.math.BigInteger;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class CommonAE2Part {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(9000000000000000000L);

    public static long insert(TileInfChest chest, long amount, ItemStack definition, boolean execute) {
        if (!chest.canInsertFromOutside(definition)) return 0; // The item is NOT acceptable.
        if (execute) {
            chest.addStack(definition, BigInteger.valueOf(amount));
            chest.setChanged();
        }
        return amount;
    }

    public static long extract(TileInfChest chest, long amount, ItemStack definition, boolean execute) {
        var holding = chest.getHolding();
        var out = chest.getItem(1);
        if (ItemStack.isSameItemSameTags(definition, holding)) {
            BigInteger extractCount = BigInteger.valueOf(amount).min(chest.totalCount());
            if (execute) {
                // do subtract.
                chest.decrStack(extractCount);
                chest.setChanged();
            }
            return extractCount.longValue();
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

    public static <Key> void getAvailableStacks(Function<ItemStack, Key> keyFactory, BiConsumer<Key, Long> out, TileInfChest chest) {
        var holding = chest.getHolding();
        if (!holding.isEmpty()) {
            var count = LONG_MAX.min(chest.totalCount());
            out.accept(Objects.requireNonNull(keyFactory.apply(holding)), count.longValue());
        }
    }

    public static boolean isPreferredStorageFor(TileInfChest chest, ItemStack toInsert) {
        return chest.canInsertFromOutside(toInsert);
    }
}
