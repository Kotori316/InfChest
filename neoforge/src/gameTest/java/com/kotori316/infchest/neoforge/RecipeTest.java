package com.kotori316.infchest.neoforge;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.RecipeTestCase;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@PrefixGameTestTemplate(value = false)
public final class RecipeTest {
    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
    public static final class Register {
        @SubscribeEvent
        public static void registerGameTest(RegisterGameTestsEvent event) {
            event.register(RecipeTest.class);
        }
    }

    static void checkRecipe(GameTestHelper helper, RecipeTestCase.RecipeSet recipeSet) {
        var manager = helper.getLevel().getServer().getRecipeManager();
        var recipe = manager.getRecipesFor(RecipeType.CRAFTING, recipeSet.input(), helper.getLevel());
        helper.assertFalse(recipe.isEmpty(), "Recipe must be found");

        for (var holder : recipe) {
            if (holder.id().equals(recipeSet.result())) {
                helper.succeed();
            }
        }
    }

    @GameTestGenerator
    public static List<TestFunction> recipeTests() {
        return RecipeTestCase.getRecipeSets()
            .stream()
            .map(r -> new TestFunction(RecipeTest.class.getSimpleName(), "recipe_test_" + r.name(), "minecraft:trail_ruins/tower/one_room_1", 10, 0, true, g -> checkRecipe(g, r)))
            .toList();
    }
}
