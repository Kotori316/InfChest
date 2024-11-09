package com.kotori316.infchest.data.common;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public final class CommonRecipe extends RecipeProvider {

    private final IngredientProvider provider;

    public CommonRecipe(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerFuture, IngredientProvider provider) {
        super(packOutput, providerFuture);
        this.provider = provider;
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, InfChest.accessor.CHEST())
            .pattern("eqe")
            .pattern("ibi")
            .pattern("sis")
            .define('e', provider.enderChest())
            .define('i', provider.ingots())
            .define('q', provider.quartzBlock())
            .define('b', Items.BEACON)
            .define('s', provider.shulkerBox())
            .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, InfChest.accessor.DEQUE())
            .pattern("c c")
            .pattern("drd")
            .pattern("c c")
            .define('c', provider.chests())
            .define('d', provider.dispenserLikes())
            .define('r', provider.rsPlates())
            .save(recipeOutput);
    }
}
