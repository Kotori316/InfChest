package com.kotori316.infchest.neoforge.tiles;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.test.DequeTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@PrefixGameTestTemplate(value = false)
public final class TileDequeTest {
    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
    public static final class Register {
        @SubscribeEvent
        public static void registerGameTest(RegisterGameTestsEvent event) {
            event.register(TileDequeTest.class);
        }
    }

    @GameTestGenerator
    public static List<TestFunction> functions() {
        return DequeTest.functions("defaultBatch", "minecraft:trail_ruins/tower/one_room_1").toList();
    }
}
