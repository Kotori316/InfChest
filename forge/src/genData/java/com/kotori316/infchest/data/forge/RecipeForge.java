package com.kotori316.infchest.data.forge;

import com.kotori316.infchest.data.common.CommonRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.util.Set;

public final class RecipeForge implements MultiRegistryBootstrap {
    @Override
    public Set<ResourceKey<? extends Registry<?>>> requestedRegistries() {
        return Set.of(Registries.RECIPE, Registries.ADVANCEMENT);
    }

    @Override
    public void run(BootstrapGetter registries) {
        var recipeContext = registries.get(Registries.RECIPE);
        var provider = new IngredientProviderForge(recipeContext);
        new CommonRecipe(recipeContext, registries.get(Registries.ADVANCEMENT), provider).buildRecipes();
    }
}
