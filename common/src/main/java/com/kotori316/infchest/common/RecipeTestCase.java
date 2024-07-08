package com.kotori316.infchest.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

public final class RecipeTestCase {
    public record RecipeSet(String name, ResourceLocation result, CraftingInput input) {
    }

    static Stream<RecipeSet> infChestRecipes() {
        var recipeSets = Stream.<RecipeSet>builder();
        var infChest = ResourceLocation.fromNamespaceAndPath(InfChest.modID, "infchest");
        var e = Items.ENDER_CHEST.getDefaultInstance();
        var q = Items.QUARTZ_BLOCK.getDefaultInstance();
        var iron = Items.IRON_INGOT.getDefaultInstance();
        var gold = Items.GOLD_INGOT.getDefaultInstance();
        var b = Items.BEACON.getDefaultInstance();

        for (DyeColor color : DyeColor.values()) {
            var s = ShulkerBoxBlock.getColoredItemStack(color);
            recipeSets.add(new RecipeSet("chest_iron_" + color.getName(), infChest, CraftingInput.of(3, 3, List.of(
                e, q, e,
                iron, b, iron,
                s, iron, s
            ))));
            recipeSets.add(new RecipeSet("chest_gold_" + color.getName(), infChest, CraftingInput.of(3, 3, List.of(
                e, q, e,
                gold, b, gold,
                s, gold, s
            ))));
            recipeSets.add(new RecipeSet("chest_ingots_" + color.getName(), infChest, CraftingInput.of(3, 3, List.of(
                e, q, e,
                gold, b, gold,
                s, iron, s
            ))));
        }
        return recipeSets.build();
    }

    static Stream<RecipeSet> dequeRecipes() {
        var recipeSets = Stream.<RecipeSet>builder();

        var result = ResourceLocation.fromNamespaceAndPath(InfChest.modID, "deque");
        var e = ItemStack.EMPTY;
        var rs = List.of(Items.REPEATER.getDefaultInstance(), Items.COMPARATOR.getDefaultInstance());
        var chests = List.of(Items.CHEST.getDefaultInstance(), Items.ENDER_CHEST.getDefaultInstance(), Items.TRAPPED_CHEST.getDefaultInstance());
        var center = List.of(Items.DROPPER.getDefaultInstance(), Items.DISPENSER.getDefaultInstance());

        for (ItemStack d : center) {
            for (ItemStack c : chests) {
                for (ItemStack r : rs) {
                    recipeSets.add(new RecipeSet("deque_%s_%s_%s".formatted(
                        d.getItem().getName(d).getString(),
                        c.getItem().getName(c).getString(),
                        r.getItem().getName(r).getString()
                    ).toLowerCase(Locale.ROOT).replace(' ', '_'), result, CraftingInput.of(3, 3, List.of(
                        c, e, c,
                        d, r, d,
                        c, e, c
                    ))));
                }
            }
        }

        return recipeSets.build();
    }

    public static List<RecipeSet> getRecipeSets() {
        return Stream.of(
                infChestRecipes(),
                dequeRecipes()
            ).flatMap(Function.identity())
            .toList();
    }
}
