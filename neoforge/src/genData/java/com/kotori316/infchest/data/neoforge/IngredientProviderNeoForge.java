package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.data.common.IngredientProvider;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

public record IngredientProviderNeoForge(HolderLookup.Provider provider) implements IngredientProvider {
    @Override
    public HolderGetter<Item> items() {
        return provider.lookupOrThrow(Registries.ITEM);
    }

    @Override
    public Ingredient enderChest() {
        return Ingredient.of(items().getOrThrow(Tags.Items.CHESTS_ENDER));
    }

    @Override
    public Ingredient ingots() {
        return CompoundIngredient.of(
            Ingredient.of(items().getOrThrow(Tags.Items.INGOTS_IRON)),
            Ingredient.of(items().getOrThrow(Tags.Items.INGOTS_GOLD))
        );
    }

    @Override
    public Ingredient quartzBlock() {
        return Ingredient.of(Items.QUARTZ_BLOCK);
    }

    @Override
    public Ingredient shulkerBox() {
        return Ingredient.of(items().getOrThrow(Tags.Items.SHULKER_BOXES));
    }

    @Override
    public Ingredient chests() {
        return Ingredient.of(items().getOrThrow(Tags.Items.CHESTS));
    }
}
