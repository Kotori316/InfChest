package com.kotori316.infchest.data.common;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public interface IngredientProvider {
    HolderGetter<Item> items();

    Ingredient enderChest();

    Ingredient ingots();

    Ingredient quartzBlock();

    Ingredient shulkerBox();

    Ingredient chests();

    default Ingredient dispenserLikes() {
        return Ingredient.of(Items.DISPENSER, Items.DROPPER);
    }

    default Ingredient rsPlates() {
        return Ingredient.of(Items.REPEATER, Items.COMPARATOR);
    }
}
