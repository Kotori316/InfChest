package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.InfChest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.stream.IntStream;

public class TileInfChestTest {
    @GameTest()
    public void placeOne(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        helper.getBlockEntity(pos, TileInfChestFabric.class);
        helper.succeed();
    }

    @GameTest()
    public void isEmpty(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        if (tile.isEmpty()) {
            helper.succeed();
        } else {
            var t = IntStream.range(0, tile.getContainerSize()).mapToObj(tile::getItem).toList();
            throw new GameTestAssertException(Component.literal("Tile must be empty, %s".formatted(t)), 1);
        }
    }

    @GameTest()
    public void isEmpty2(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());
        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var stack = new ItemStack(Items.APPLE, 10);
        tile.setItem(0, stack);
        var removed = tile.removeItem(1, 10);
        if (!ItemStack.isSameItemSameComponents(stack, removed)) {
            throw new GameTestAssertException(Component.literal("Removed item(%s) and inserted item(%s) must be same.".formatted(removed, stack)), 1);
        }
        if (tile.isEmpty()) {
            helper.succeed();
        } else {
            var t = IntStream.range(0, tile.getContainerSize()).mapToObj(tile::getItem).toList();
            throw new GameTestAssertException(Component.literal("Tile must be empty, %s".formatted(t)), 1);
        }
    }

    @GameTest()
    public void setItem(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());
        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var stack = new ItemStack(Items.APPLE, 16);
        tile.setItem(0, stack);
        if (tile.getItem(0).isEmpty()) {
            helper.succeed();
        } else {
            throw new GameTestAssertException(Component.literal("Slot 0 must be empty after inserting, but %s".formatted(tile.getItem(0))), 1);
        }
    }

    @GameTest()
    public void addItem1(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        tile.setItem(0, new ItemStack(Items.APPLE, 4));

        var stack = tile.getItem(1);
        if (ItemStack.matches(stack, new ItemStack(Items.APPLE, 4))) {
            helper.succeed();
        } else {
            throw new GameTestAssertException(Component.literal("Tile has unexpected item, %s".formatted(stack)), 1);
        }
    }

    @GameTest()
    public void addItem2(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        tile.setItem(0, new ItemStack(Items.APPLE, 4));
        tile.setItem(0, new ItemStack(Items.APPLE, 64));

        var stack = tile.getItem(1);
        if (!ItemStack.matches(stack, new ItemStack(Items.APPLE, 64))) {
            throw new GameTestAssertException(Component.literal("Tile has unexpected item, %s, %s".formatted(stack, tile.totalCount())), 1);
        }
        CheckHelper.checkTotalCount(helper, tile, 64 + 4);
        helper.succeed();
    }

    @GameTest()
    public void addItem3(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        tile.setItem(0, new ItemStack(Items.APPLE, 64));
        tile.setItem(0, new ItemStack(Items.APPLE, 64));
        tile.setItem(0, new ItemStack(Items.APPLE, 32));

        if (!ItemStack.isSameItemSameComponents(tile.getItem(1), new ItemStack(Items.APPLE, 64))) {
            throw new GameTestAssertException(Component.literal("Tile has unexpected item, %s".formatted(tile.getItem(1))), 1);
        }
        if (!tile.getItem(0).isEmpty()) {
            throw new GameTestAssertException(Component.literal("Tile has unexpected item, %s".formatted(tile.getItem(0))), 1);
        }
        if (!ItemStack.isSameItemSameComponents(new ItemStack(Items.APPLE), tile.getHolding())) {
            throw new GameTestAssertException(Component.literal("Holding of tile, Actual: %s, Expected: %s".formatted(tile.getHolding(), Items.APPLE)), 1);
        }
        helper.succeed();
    }

    @GameTest()
    public void addItem4(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        enchantedBook.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), 2);
        tile.setItem(0, enchantedBook);

        var stack = tile.getItem(1);
        if (ItemStack.matches(stack, enchantedBook)) {
            helper.succeed();
        } else {
            throw new GameTestAssertException(Component.literal("Tile has unexpected item, %s".formatted(stack)), 1);
        }
    }

    @GameTest()
    public void takeItem1(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var stack = new ItemStack(Items.APPLE, 64);
        tile.setItem(0, stack.copy());
        tile.setItem(0, stack.copy());
        tile.setItem(0, new ItemStack(Items.APPLE, 32));

        CheckHelper.checkTotalCount(helper, tile, 160);

        var removed = tile.removeItem(1, 64);
        if (!ItemStack.isSameItemSameComponents(removed, stack)) {
            throw new GameTestAssertException(Component.literal("%s must be taken. %s".formatted(stack, removed)), 1);
        }
        tile.setChanged();
        var out = tile.getItem(1);
        if (!ItemStack.matches(out, stack)) {
            throw new GameTestAssertException(Component.literal("Out slot must be updated. %s".formatted(out)), 1);
        }
        CheckHelper.checkTotalCount(helper, tile, 96);

        helper.succeed();
    }

}
