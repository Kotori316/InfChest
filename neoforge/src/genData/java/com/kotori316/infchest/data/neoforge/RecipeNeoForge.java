package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.data.common.CommonRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public final class RecipeNeoForge extends RecipeProvider.Runner {

    public RecipeNeoForge(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture) {
        super(packOutput, providerCompletableFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider holderLookup, RecipeOutput recipeOutput) {
        var provider = new IngredientProviderNeoForge(holderLookup);
        return new CommonRecipe(holderLookup, recipeOutput, provider);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
