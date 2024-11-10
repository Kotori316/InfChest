package com.kotori316.infchest.data.forge;

import com.kotori316.infchest.data.common.IngredientProvider;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ingredients.CompoundIngredient;

public record IngredientProviderForge(HolderLookup.Provider provider) implements IngredientProvider {
    @Override
    public HolderGetter<Item> items() {
        return provider.lookupOrThrow(Registries.ITEM);
    }

    @Override
    public Ingredient enderChest() {
        return Ingredient.of(Tags.Items.CHESTS_ENDER);
    }

    @Override
    public Ingredient ingots() {
        return CompoundIngredient.of(
            Ingredient.of(Tags.Items.INGOTS_IRON),
            Ingredient.of(Tags.Items.INGOTS_GOLD)
        );
    }

    @Override
    public Ingredient quartzBlock() {
        return Ingredient.of(Tags.Items.STORAGE_BLOCKS_QUARTZ);
    }

    @Override
    public Ingredient shulkerBox() {
        return Ingredient.of(Tags.Items.SHULKER_BOXES);
    }

    @Override
    public Ingredient chests() {
        return Ingredient.of(Tags.Items.CHESTS);
    }
}
