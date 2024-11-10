package com.kotori316.infchest.data.fabric;

import com.kotori316.infchest.data.common.IngredientProvider;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientProviderFabric(HolderLookup.Provider provider) implements IngredientProvider {
    @Override
    public HolderGetter<Item> items() {
        return provider.lookupOrThrow(Registries.ITEM);
    }

    @Override
    public Ingredient enderChest() {
        return Ingredient.of(Items.ENDER_CHEST);
    }

    @Override
    public Ingredient ingots() {
        return DefaultCustomIngredients.any(Ingredient.of(ConventionalItemTags.IRON_INGOTS), Ingredient.of(ConventionalItemTags.GOLD_INGOTS));
    }

    @Override
    public Ingredient quartzBlock() {
        return Ingredient.of(Items.QUARTZ_BLOCK);
    }

    @Override
    public Ingredient shulkerBox() {
        return Ingredient.of(ConventionalItemTags.SHULKER_BOXES);
    }

    @Override
    public Ingredient chests() {
        return Ingredient.of(ConventionalItemTags.CHESTS);
    }
}
