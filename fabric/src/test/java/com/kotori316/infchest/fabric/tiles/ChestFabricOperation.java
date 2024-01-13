package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.InfChest;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.math.BigInteger;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class ChestFabricOperation implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void insertViaStorage(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
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
                throw new GameTestAssertException("Insertion failed. %d".formatted(inserted));
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
                throw new GameTestAssertException("Insertion failed. %d".formatted(inserted));
            }
            CheckHelper.checkTotalCount(helper, tile, 2000 + i);
            transaction.commit();
        }
        CheckHelper.checkTotalCount(helper, tile, 2000 + i);

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void insertViaStorageInvalidItem(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var item = Items.CRIMSON_PLANKS;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(2000));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var inserted = storage.insert(ItemVariant.of(Items.APPLE), 10, transaction);
            if (inserted != 0) {
                throw new GameTestAssertException("Invalid items were inserted. %d".formatted(inserted));
            }
            // abort
        }
        CheckHelper.checkTotalCount(helper, tile, 2000);
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void insertViaStorageToEmpty(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var storage = new InfChestStorage(tile);

        // Trial 1
        {
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = storage.insert(ItemVariant.of(Items.APPLE), 300, transaction);
                if (inserted != 300) {
                    throw new GameTestAssertException("1: Insertion failed. %d".formatted(inserted));
                }
                if (!ItemStack.isSameItemSameTags(new ItemStack(Items.APPLE), tile.getHolding())) {
                    throw new GameTestAssertException("1: Invalid items were inserted. %s".formatted(tile.getHolding()));
                }
                transaction.abort();
            }
            if (!tile.getHolding().isEmpty() && !tile.isEmpty()) {
                throw new GameTestAssertException("1: Abort failed. %s".formatted(tile.getHolding()));
            }
        }
        // Trial 2
        {
            try (Transaction transaction = Transaction.openOuter()) {
                var inserted = storage.insert(ItemVariant.of(Items.APPLE), 300, transaction);
                tile.setChanged();
                if (inserted != 300) {
                    throw new GameTestAssertException("2: Insertion failed. %d".formatted(inserted));
                }
                if (!ItemStack.isSameItemSameTags(new ItemStack(Items.APPLE), tile.getHolding())) {
                    throw new GameTestAssertException("2: Invalid items were inserted. %s".formatted(tile.getHolding()));
                }
                transaction.abort();
            }
            if (!tile.getHolding().isEmpty() && !tile.isEmpty()) {
                throw new GameTestAssertException("2: Abort failed. %s".formatted(tile.getHolding()));
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

    @GameTest(template = EMPTY_STRUCTURE)
    public void extractViaStorageFromEmpty(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var extracted = storage.extract(ItemVariant.of(Items.APPLE), 10, transaction);
            if (extracted != 0) {
                throw new GameTestAssertException("What item did you extracted? " + extracted);
            } else {
                helper.succeed();
            }
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void extractViaStorage(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var item = Items.CRIMSON_PLANKS;
        var initial = 2000;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var i = 10;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException("Extraction failed. " + extracted);
            }
            CheckHelper.checkTotalCount(helper, tile, initial - i);
            transaction.abort();
        }
        CheckHelper.checkTotalCount(helper, tile, initial);
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 100;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException("Extraction failed. %d, expected: %d".formatted(extracted, i));
            }
            CheckHelper.checkTotalCount(helper, tile, initial - i);
            transaction.commit();
        }
        CheckHelper.checkTotalCount(helper, tile, initial - 100);

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void extractViaStorage2(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var item = Items.CRIMSON_PLANKS;
        var initial = 2064;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var i = 2000;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException("Extraction failed. %d, expected: %d".formatted(extracted, i));
            }
            transaction.commit();
            tile.setChanged();
        }
        if (!tile.getHolding().is(item)) {
            throw new GameTestAssertException("Holding must be valid item. " + tile.getHolding());
        }
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(item, 64))) {
            throw new GameTestAssertException("Output slot. " + tile.getItem(1));
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void extractViaStorage3(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
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
                throw new GameTestAssertException("Extraction failed. %d, expected: %d".formatted(extracted, i));
            }
            transaction.commit();
            tile.setChanged();
        }
        if (!tile.getHolding().is(item)) {
            throw new GameTestAssertException("Holding must be valid item. " + tile.getHolding());
        }
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(item, 32))) {
            throw new GameTestAssertException("Output slot. " + tile.getItem(1));
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void extractViaStorage4(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var item = Items.CRIMSON_PLANKS;
        var initial = 2064;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
        tile.setChanged();
        var storage = new InfChestStorage(tile);

        try (Transaction transaction = Transaction.openOuter()) {
            var i = 2032;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException("Extraction failed. %d, expected: %d".formatted(extracted, i));
            }
            CheckHelper.checkTotalCount(helper, tile, initial - i);
            transaction.commit();
            tile.setChanged();
        }
        if (tile.getHolding().isEmpty()) {
            throw new GameTestAssertException("Holding must not be empty. " + tile.getHolding());
        }
        CheckHelper.checkTotalCount(helper, tile, 32);
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(item, 32))) {
            throw new GameTestAssertException("Output slot. " + tile.getItem(1));
        }
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 40;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != 32) {
                throw new GameTestAssertException("Extraction failed. %d, expected: %d".formatted(extracted, i));
            }
            transaction.commit();
            tile.setChanged();
        }
        CheckHelper.checkTotalCount(helper, tile, 0);
        if (!tile.getHolding().isEmpty()) {
            throw new GameTestAssertException("Tile must be empty after extracting");
        }
        if (!tile.isEmpty()) {
            throw new GameTestAssertException("Tile must be empty after extracting");
        }
        helper.succeed();
    }
}
