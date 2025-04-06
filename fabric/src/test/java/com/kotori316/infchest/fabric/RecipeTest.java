package com.kotori316.infchest.fabric;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.test.RecipeTestCase;
import com.kotori316.testutil.common.TestFunction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.List;

public final class RecipeTest {
    static void checkRecipe(GameTestHelper helper, RecipeTestCase.RecipeSet recipeSet) {
        var manager = helper.getLevel().getServer().getRecipeManager();
        var recipe = manager.getRecipeFor(RecipeType.CRAFTING, recipeSet.input(), helper.getLevel());
        helper.assertTrue(recipe.isPresent(), Component.literal("Recipe must be found"));

        var location = recipe.map(RecipeHolder::id).map(ResourceKey::location).orElse(null);
        if (recipeSet.result().equals(location)) {
            helper.succeed();
        }
    }

    public static List<TestFunction> recipeTests() {
        return RecipeTestCase.getRecipeSets()
            .stream()
            .map(r -> TestFunction.create(InfChest.modID, InfChest.modID + ":test", "recipe_test_" + r.name(), g -> checkRecipe(g, r)))
            .toList();
    }
}
