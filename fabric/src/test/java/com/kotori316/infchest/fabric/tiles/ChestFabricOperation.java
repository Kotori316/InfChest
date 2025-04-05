package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.InfChest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.math.BigInteger;

public final class ChestFabricOperation {

    @GameTest()
    public void insertViaStorage(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var item = Items.CRIMSON_PLANKS;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(2000));
        tile.setChanged();
        CheckHelper.checkTotalCount(helper, tile, 2000);

        var storage = new InfChestStorage(tile);
        // Simulation
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 200;
            var inserted = storage.insert(ItemVariant.of(item), i, transaction);
            if (inserted != i) {
                throw new GameTestAssertException(Component.literal("Insertion failed. %d".formatted(inserted)), 1);
            }
            CheckHelper.checkTotalCount(helper, tile, 2000 + i);
            transaction.abort();
        }
        CheckHelper.checkTotalCount(helper, tile, 2000);

        // Execution
        var i = 400;
        try (Transaction transaction = Transaction.openOuter()) {
            var inserted = storage.insert(ItemVariant.of(item), i, transaction);
            if (inserted != i) {
                throw new GameTestAssertException(Component.literal("Insertion failed. %d".formatted(inserted)), 1);
            }
            CheckHelper.checkTotalCount(helper, tile, 2000 + i);
            transaction.commit();
        }
        CheckHelper.checkTotalCount(helper, tile, 2000 + i);

        helper.succeed();
    }

    @GameTest()
    public void insertViaStorageInvalidItem(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var item = Items.CRIMSON_PLANKS;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(2000));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var inserted = storage.insert(ItemVariant.of(Items.APPLE), 10, transaction);
            if (inserted != 0) {
                throw new GameTestAssertException(Component.literal("Invalid items were inserted. %d".formatted(inserted)), 1);
            }
            // abort
        }
        CheckHelper.checkTotalCount(helper, tile, 2000);
        helper.succeed();
    }

    @GameTest()
    public void insertViaStorageToEmpty(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var storage = new InfChestStorage(tile);

        // Trial 1
        {
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = storage.insert(ItemVariant.of(Items.APPLE), 300, transaction);
                if (inserted != 300) {
                    throw new GameTestAssertException(Component.literal("1: Insertion failed. %d".formatted(inserted)), 1);
                }
                if (!ItemStack.isSameItemSameComponents(new ItemStack(Items.APPLE), tile.getHolding())) {
                    throw new GameTestAssertException(Component.literal("1: Invalid items were inserted. %s".formatted(tile.getHolding())), 1);
                }
                transaction.abort();
            }
            if (!tile.getHolding().isEmpty() && !tile.isEmpty()) {
                throw new GameTestAssertException(Component.literal("1: Abort failed. %s".formatted(tile.getHolding())), 1);
            }
        }
        // Trial 2
        {
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = storage.insert(ItemVariant.of(Items.APPLE), 300, transaction);
                tile.setChanged();
                if (inserted != 300) {
                    throw new GameTestAssertException(Component.literal("2: Insertion failed. %d".formatted(inserted)), 1);
                }
                if (!ItemStack.isSameItemSameComponents(new ItemStack(Items.APPLE), tile.getHolding())) {
                    throw new GameTestAssertException(Component.literal("2: Invalid items were inserted. %s".formatted(tile.getHolding())), 1);
                }
                transaction.abort();
            }
            if (!tile.getHolding().isEmpty() && !tile.isEmpty()) {
                throw new GameTestAssertException(Component.literal("2: Abort failed. %s".formatted(tile.getHolding())), 1);
            }
        }

        try (Transaction transaction = Transaction.openOuter()) {
            storage.insert(ItemVariant.of(Items.APPLE), 300, transaction);
            transaction.commit();
        }
        tile.setChanged();
        CheckHelper.checkTotalCount(helper, tile, 300);
        helper.succeed();
    }

    @GameTest()
    public void extractViaStorageFromEmpty(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var extracted = storage.extract(ItemVariant.of(Items.APPLE), 10, transaction);
            if (extracted != 0) {
                throw new GameTestAssertException(Component.literal("What item did you extracted? " + extracted), 1);
            } else {
                helper.succeed();
            }
        }
    }

    @GameTest()
    public void extractViaStorage(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var item = Items.CRIMSON_PLANKS;
        var initial = 2000;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var i = 10;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException(Component.literal("Extraction failed. " + extracted), 1);
            }
            CheckHelper.checkTotalCount(helper, tile, initial - i);
            transaction.abort();
        }
        CheckHelper.checkTotalCount(helper, tile, initial);
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 100;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException(Component.literal("Extraction failed. %d, expected: %d".formatted(extracted, i)), 1);
            }
            CheckHelper.checkTotalCount(helper, tile, initial - i);
            transaction.commit();
        }
        CheckHelper.checkTotalCount(helper, tile, initial - 100);

        helper.succeed();
    }

    @GameTest()
    public void extractViaStorage2(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var item = Items.CRIMSON_PLANKS;
        var initial = 2064;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var i = 2000;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException(Component.literal("Extraction failed. %d, expected: %d".formatted(extracted, i)), 1);
            }
            transaction.commit();
            tile.setChanged();
        }
        if (!tile.getHolding().is(item)) {
            throw new GameTestAssertException(Component.literal("Holding must be valid item. " + tile.getHolding()), 1);
        }
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(item, 64))) {
            throw new GameTestAssertException(Component.literal("Output slot. " + tile.getItem(1)), 1);
        }
        helper.succeed();
    }

    @GameTest()
    public void extractViaStorage3(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var item = Items.CRIMSON_PLANKS;
        var initial = 2064;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
        tile.setChanged();
        CheckHelper.checkTotalCount(helper, tile, initial);

        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var i = 2032;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException(Component.literal("Extraction failed. %d, expected: %d".formatted(extracted, i)), 1);
            }
            transaction.commit();
            tile.setChanged();
        }
        if (!tile.getHolding().is(item)) {
            throw new GameTestAssertException(Component.literal("Holding must be valid item. " + tile.getHolding()), 1);
        }
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(item, 32))) {
            throw new GameTestAssertException(Component.literal("Output slot. " + tile.getItem(1)), 1);
        }
        helper.succeed();
    }

    @GameTest()
    public void extractViaStorage4(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = helper.getBlockEntity(pos, TileInfChestFabric.class);
        var item = Items.CRIMSON_PLANKS;
        var initial = 2064;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var i = 2032;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException(Component.literal("Extraction failed. %d, expected: %d".formatted(extracted, i)), 1);
            }
            CheckHelper.checkTotalCount(helper, tile, initial - i);
            transaction.commit();
            tile.setChanged();
        }
        if (tile.getHolding().isEmpty()) {
            throw new GameTestAssertException(Component.literal("Holding must not be empty. " + tile.getHolding()), 1);
        }
        CheckHelper.checkTotalCount(helper, tile, 32);
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(item, 32))) {
            throw new GameTestAssertException(Component.literal("Output slot. " + tile.getItem(1)), 1);
        }
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 40;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != 32) {
                throw new GameTestAssertException(Component.literal("Extraction failed. %d, expected: %d".formatted(extracted, i)), 1);
            }
            transaction.commit();
            tile.setChanged();
        }
        CheckHelper.checkTotalCount(helper, tile, 0);
        if (!tile.getHolding().isEmpty()) {
            throw new GameTestAssertException(Component.literal("Tile must be empty after extracting"), 1);
        }
        if (!tile.isEmpty()) {
            throw new GameTestAssertException(Component.literal("Tile must be empty after extracting"), 1);
        }
        helper.succeed();
    }
}
