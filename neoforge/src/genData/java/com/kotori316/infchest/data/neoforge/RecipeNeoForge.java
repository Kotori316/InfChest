package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.data.common.CommonRecipe;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;

import java.util.Set;

public final class RecipeNeoForge implements MultiRegistryBootstrap {
    @Override
    public Set<ResourceKey<? extends Registry<?>>> requestedRegistries() {
        return Set.of(Registries.RECIPE, Registries.ADVANCEMENT);
    }

    @Override
    public void run(BootstrapGetter registries) {
        var recipeContext = registries.get(Registries.RECIPE);
        var provider = new IngredientProviderNeoForge(recipeContext);
        var recipeProvider = new CommonRecipe(recipeContext, registries.get(Registries.ADVANCEMENT), provider);
        recipeProvider.buildRecipes();
    }
}
