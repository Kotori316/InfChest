package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.InfChest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Objects;
import java.util.stream.IntStream;

public class TileInfChestTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void placeOne(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos);
        if (tile instanceof TileInfChestFabric) {
            helper.succeed();
        } else {
            throw new GameTestAssertException("Unknown tile exists, %s".formatted(tile));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void isEmpty(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) helper.getBlockEntity(pos);
        assert tile != null;
        if (tile.isEmpty()) {
            helper.succeed();
        } else {
            var t = IntStream.range(0, tile.getContainerSize()).mapToObj(tile::getItem).toList();
            throw new GameTestAssertException("Tile must be empty, %s".formatted(t));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void isEmpty2(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());
        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var stack = new ItemStack(Items.APPLE, 10);
        tile.setItem(0, stack);
        var removed = tile.removeItem(1, 10);
        if (!ItemStack.isSame(stack, removed)) {
            throw new GameTestAssertException("Removed item(%s) and inserted item(%s) must be same.".formatted(removed, stack));
        }
        if (tile.isEmpty()) {
            helper.succeed();
        } else {
            var t = IntStream.range(0, tile.getContainerSize()).mapToObj(tile::getItem).toList();
            throw new GameTestAssertException("Tile must be empty, %s".formatted(t));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void setItem(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());
        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var stack = new ItemStack(Items.APPLE, 16);
        tile.setItem(0, stack);
        if (tile.getItem(0).isEmpty()) {
            helper.succeed();
        } else {
            throw new GameTestAssertException("Slot 0 must be empty after inserting, but %s".formatted(tile.getItem(0)));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void addItem1(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        tile.setItem(0, new ItemStack(Items.APPLE, 4));

        var stack = tile.getItem(1);
        if (ItemStack.matches(stack, new ItemStack(Items.APPLE, 4))) {
            helper.succeed();
        } else {
            throw new GameTestAssertException("Tile has unexpected item, %s".formatted(stack));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void addItem2(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        tile.setItem(0, new ItemStack(Items.APPLE, 4));
        tile.setItem(0, new ItemStack(Items.APPLE, 64));

        var stack = tile.getItem(1);
        if (!ItemStack.matches(stack, new ItemStack(Items.APPLE, 64))) {
            throw new GameTestAssertException("Tile has unexpected item, %s, %s".formatted(stack, tile.totalCount()));
        }
        CheckHelper.checkTotalCount(helper, tile, 64 + 4);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void addItem3(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        tile.setItem(0, new ItemStack(Items.APPLE, 64));
        tile.setItem(0, new ItemStack(Items.APPLE, 64));
        tile.setItem(0, new ItemStack(Items.APPLE, 32));

        if (!ItemStack.isSame(tile.getItem(1), new ItemStack(Items.APPLE, 64))) {
            throw new GameTestAssertException("Tile has unexpected item, %s".formatted(tile.getItem(1)));
        }
        if (!tile.getItem(0).isEmpty()) {
            throw new GameTestAssertException("Tile has unexpected item, %s".formatted(tile.getItem(0)));
        }
        if (!ItemStack.isSameItemSameTags(new ItemStack(Items.APPLE), tile.getHolding())) {
            throw new GameTestAssertException("Holding of tile, Actual: %s, Expected: %s".formatted(tile.getHolding(), Items.APPLE));
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void takeItem1(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var stack = new ItemStack(Items.APPLE, 64);
        tile.setItem(0, stack.copy());
        tile.setItem(0, stack.copy());
        tile.setItem(0, new ItemStack(Items.APPLE, 32));

        CheckHelper.checkTotalCount(helper, tile, 160);

        var removed = tile.removeItem(1, 64);
        if (!ItemStack.isSame(removed, stack)) {
            throw new GameTestAssertException("%s must be taken. %s".formatted(stack, removed));
        }
        tile.setChanged();
        var out = tile.getItem(1);
        if (!ItemStack.matches(out, stack)) {
            throw new GameTestAssertException("Out slot must be updated. %s".formatted(out));
        }
        CheckHelper.checkTotalCount(helper, tile, 96);

        helper.succeed();
    }

}
