package com.kotori316.infchest.neoforge;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.test.DequeTest;
import com.kotori316.testutil.common.TestFunction;
import com.kotori316.testutil.common.TestFunctionRegister;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

import java.util.function.Function;
import java.util.stream.Stream;

@EventBusSubscriber(modid = InfChest.modID)
public final class InfChestNeoForgeGameTest {
    @SubscribeEvent
    public static void registerGameTest(FMLConstructModEvent event) {
        var dequeTests = DequeTest.functions(InfChest.modID + ":test", "minecraft:trail_ruins/tower/one_room_1")
            .map(r -> TestFunction.createWithStructure(InfChest.modID, r.batchName(), r.name(), r.structure(), r.function()));
        var recipeTests = RecipeTest.recipeTests().stream();
        Stream.of(dequeTests, recipeTests)
            .flatMap(Function.identity())
            .forEach(TestFunctionRegister::registerTestFunction);
    }
}
