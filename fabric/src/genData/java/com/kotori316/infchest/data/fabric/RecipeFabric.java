package com.kotori316.infchest.data.fabric;

import com.kotori316.infchest.data.common.CommonRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public final class RecipeFabric extends FabricRecipeProvider {
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;
    private HolderLookup.Provider caught = null;

    public RecipeFabric(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
        this.registriesFuture = registriesFuture;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer, HolderLookup.Provider wrapperLookup) {
        caught = wrapperLookup;
        return super.run(writer, wrapperLookup);
    }

    @Override
    public void buildRecipes(RecipeOutput exporter) {
        var internal = new CommonRecipe(this.output, this.registriesFuture, new IngredientProviderFabric(this.caught));
        internal.buildRecipes(exporter);
    }

    @Override
    protected ResourceLocation getRecipeIdentifier(ResourceLocation identifier) {
        return identifier;
    }
}
