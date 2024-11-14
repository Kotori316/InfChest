package com.kotori316.infchest.fabric;

import com.kotori316.infchest.common.RecipeTestCase;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public final class RecipeTest implements FabricGameTest {
    void checkRecipe(GameTestHelper helper, RecipeTestCase.RecipeSet recipeSet) {
        var manager = helper.getLevel().getServer().getRecipeManager();
        var recipe = manager.getRecipeFor(RecipeType.CRAFTING, recipeSet.input(), helper.getLevel());
        helper.assertTrue(recipe.isPresent(), "Recipe must be found");

        var location = recipe.map(RecipeHolder::id).map(ResourceKey::location).orElse(null);
        if (recipeSet.result().equals(location)) {
            helper.succeed();
        }
    }

    @GameTestGenerator
    public List<TestFunction> recipeTests() {
        return RecipeTestCase.getRecipeSets()
            .stream()
            .map(r -> new TestFunction(getClass().getSimpleName(), "recipe_test_" + r.name(), EMPTY_STRUCTURE, 10, 0, true, g -> checkRecipe(g, r)))
            .toList();
    }
}
