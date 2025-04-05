package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

import java.math.BigInteger;

class CheckHelper {
    static void checkTotalCount(GameTestHelper helper, TileInfChest chest, BigInteger expected) {
        if (!chest.totalCount().equals(expected)) {
            throw new GameTestAssertPosException(
                Component.literal("ItemCount, A: %s, E: %s".formatted(chest.totalCount(), expected)),
                chest.getBlockPos(),
                helper.relativePos(chest.getBlockPos()),
                (int) helper.getTick()
            );
        }
    }

    static void checkTotalCount(GameTestHelper helper, TileInfChest chest, long expected) {
        checkTotalCount(helper, chest, BigInteger.valueOf(expected));
    }
}
