package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.test.DequeTest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;

import java.util.List;

public final class TileDequeTest implements FabricGameTest {
    @GameTestGenerator
    public List<TestFunction> functions() {
        return DequeTest.functions("defaultBatch", EMPTY_STRUCTURE).toList();
    }
}
