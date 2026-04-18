package com.kotori316.infchest.data.forge;

import com.kotori316.infchest.data.common.CommonRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.common.data.ForgeRecipeProvider;

import java.util.concurrent.CompletableFuture;

public final class RecipeForge extends ForgeRecipeProvider.Runner {
    public RecipeForge(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider holderLookup, RecipeOutput recipeOutput) {
        var provider = new IngredientProviderForge(holderLookup);
        return new CommonRecipe(holderLookup, recipeOutput, provider);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }
}
