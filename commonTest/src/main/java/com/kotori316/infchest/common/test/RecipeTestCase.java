package com.kotori316.infchest.common.test;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class RecipeTestCase {
    public record RecipeSet(String name, Identifier result, Supplier<CraftingInput> inputSupplier) {
        public CraftingInput input() {
            return inputSupplier.get();
        }
    }

    static Stream<RecipeSet> infChestRecipes() {
        var recipeSets = Stream.<RecipeSet>builder();
        var infChest = Identifier.fromNamespaceAndPath(InfChest.modID, "infchest");
        var e = new ItemStackTemplate(Items.ENDER_CHEST);
        var q = new ItemStackTemplate(Items.QUARTZ_BLOCK);
        var iron = new ItemStackTemplate(Items.IRON_INGOT);
        var gold = new ItemStackTemplate(Items.GOLD_INGOT);
        var b = new ItemStackTemplate(Items.BEACON);

        for (DyeColor color : DyeColor.values()) {
            var s = new ItemStackTemplate(BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("minecraft", color.getName() + "_shulker_box")));
            recipeSets.add(new RecipeSet("chest_iron_" + color.getName(), infChest, () -> CraftingInput.of(3, 3, List.of(
                e.create(), q.create(), e.create(),
                iron.create(), b.create(), iron.create(),
                s.create(), iron.create(), s.create()
            ))));
            recipeSets.add(new RecipeSet("chest_gold_" + color.getName(), infChest, () -> CraftingInput.of(3, 3, List.of(
                e.create(), q.create(), e.create(),
                gold.create(), b.create(), gold.create(),
                s.create(), gold.create(), s.create()
            ))));
            recipeSets.add(new RecipeSet("chest_ingots_" + color.getName(), infChest, () -> CraftingInput.of(3, 3, List.of(
                e.create(), q.create(), e.create(),
                gold.create(), b.create(), gold.create(),
                s.create(), iron.create(), s.create()
            ))));
        }
        return recipeSets.build();
    }

    static Stream<RecipeSet> dequeRecipes() {
        var recipeSets = Stream.<RecipeSet>builder();

        var result = Identifier.fromNamespaceAndPath(InfChest.modID, "deque");
        var rs = List.of(new ItemStackTemplate(Items.REPEATER), new ItemStackTemplate(Items.COMPARATOR));
        var chests = List.of(new ItemStackTemplate(Items.CHEST), new ItemStackTemplate(Items.ENDER_CHEST), new ItemStackTemplate(Items.TRAPPED_CHEST));
        var center = List.of(new ItemStackTemplate(Items.DROPPER), new ItemStackTemplate(Items.DISPENSER));

        for (ItemStackTemplate d : center) {
            for (ItemStackTemplate c : chests) {
                for (ItemStackTemplate r : rs) {
                    recipeSets.add(new RecipeSet("deque_%s_%s_%s".formatted(
                        d.item().unwrapKey().map(ResourceKey::identifier).map(Identifier::getPath).orElseThrow(),
                        c.item().unwrapKey().map(ResourceKey::identifier).map(Identifier::getPath).orElseThrow(),
                        r.item().unwrapKey().map(ResourceKey::identifier).map(Identifier::getPath).orElseThrow()
                    ).toLowerCase(Locale.ROOT).replace(' ', '_'), result, () -> CraftingInput.of(3, 3, List.of(
                        c.create(), ItemStack.EMPTY, c.create(),
                        d.create(), r.create(), d.create(),
                        c.create(), ItemStack.EMPTY, c.create()
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
