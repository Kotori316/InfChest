package com.kotori316.infchest.common.test;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.function.Consumer;
import java.util.stream.Stream;

public final class InfChestTest {

    public record TestFunctionRecord(String batchName, String name, String structure, int tick, int setUp,
                                     boolean required, Consumer<GameTestHelper> function) {
    }

    public static Stream<TestFunctionRecord> functions(String batchName, String structure) {
        return Stream.of(
            new TestFunctionRecord(batchName, "placeInfChest", structure, 100, 0, true, InfChestTest::placeInfChest)
        );
    }

    static void placeInfChest(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        helper.getBlockEntity(pos, TileInfChest.class);
        helper.succeed();
    }

}
