package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.data.common.CommonRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public final class RecipeNeoForge extends RecipeProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    public RecipeNeoForge(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> providerCompletableFuture) {
        super(packOutput, providerCompletableFuture);
        this.output = packOutput;
        this.registriesFuture = providerCompletableFuture;
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput, HolderLookup.Provider holderLookup) {
        var internal = new CommonRecipe(this.output, this.registriesFuture, new IngredientProviderNeoForge(holderLookup));
        internal.buildRecipes(recipeOutput);
    }
}
