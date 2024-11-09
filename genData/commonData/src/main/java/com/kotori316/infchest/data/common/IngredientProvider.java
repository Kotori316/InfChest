package com.kotori316.infchest.data.common;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public interface IngredientProvider {
    HolderGetter<Item> items();

    Ingredient enderChest();

    Ingredient ingots();

    Ingredient quartzBlock();

    Ingredient shulkerBox();

    Ingredient chests();

    Ingredient dispenserLikes();

    Ingredient rsPlates();
}
