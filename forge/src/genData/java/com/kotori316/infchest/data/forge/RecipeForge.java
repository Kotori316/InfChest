package com.kotori316.infchest.data.forge;

import com.kotori316.infchest.data.common.CommonRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.concurrent.CompletableFuture;

public final class RecipeForge extends RecipeProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private HolderLookup.Provider caught = null;

    public RecipeForge(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
        this.output = pOutput;
        this.registriesFuture = pRegistries;
    }

    @Override
    protected CompletableFuture<?> run(CachedOutput pOutput, HolderLookup.Provider pRegistries) {
        caught = pRegistries;
        return super.run(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        var internal = new CommonRecipe(output, registriesFuture, new IngredientProviderForge(caught));
        internal.buildRecipes(pRecipeOutput);
    }
}
