package com.kotori316.infchest.data.common;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

public final class CommonRecipe extends RecipeProvider {

    private final IngredientProvider provider;

    public CommonRecipe(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput, IngredientProvider provider) {
        super(recipeOutput, advancementOutput);
        this.provider = provider;
    }

    @Override
    public void buildRecipes() {
        shaped(RecipeCategory.MISC, InfChest.accessor.CHEST())
            .pattern("eqe")
            .pattern("ibi")
            .pattern("sis")
            .define('e', provider.enderChest())
            .define('i', provider.ingots())
            .define('q', provider.quartzBlock())
            .define('b', Items.BEACON)
            .define('s', provider.shulkerBox())
            .unlockedBy("has_beacon", has(Items.BEACON))
            .save(this.output);

        shaped(RecipeCategory.MISC, InfChest.accessor.DEQUE())
            .pattern("c c")
            .pattern("drd")
            .pattern("c c")
            .define('c', provider.chests())
            .define('d', provider.dispenserLikes())
            .define('r', provider.rsPlates())
            .unlockedBy("has_dispenser", has(Items.DISPENSER))
            .unlockedBy("has_dropper", has(Items.DROPPER))
            .save(this.output);
    }
}
