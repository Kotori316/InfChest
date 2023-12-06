package com.kotori316.infchest.fabric.tiles;

import java.math.BigInteger;
import java.util.Objects;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.kotori316.infchest.common.InfChest;

public final class ChestFabricOperation implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void insertViaStorage(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.CHEST());

        var tile = (TileInfChestFabric) Objects.requireNonNull(helper.getBlockEntity(pos));
        var item = Items.CRIMSON_PLANKS;
        tile.addStack(new ItemStack(item), BigInteger.valueOf(2000));
        tile.setChanged();
        if (!tile.itemCount().equals(BigInteger.valueOf(2000 - 64))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 2000 - 64));
        }

        var storage = new InfChestStorage(tile);
        // Simulation
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 200;
            var inserted = storage.insert(ItemVariant.of(item), i, transaction);
            if (inserted != i) {
                throw new GameTestAssertException("Insertion failed. %d".formatted(inserted));
            }
            if (!tile.itemCount().equals(BigInteger.valueOf(2000 - 64 + i))) {
                throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 2000 - 64 + i));
            }
            transaction.abort();
        }
        if (!tile.itemCount().equals(BigInteger.valueOf(2000 - 64))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 2000 - 64));
        }

        // Execution
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 400;
            var inserted = storage.insert(ItemVariant.of(item), i, transaction);
            if (inserted != i) {
                throw new GameTestAssertException("Insertion failed. %d".formatted(inserted));
            }
            if (!tile.itemCount().equals(BigInteger.valueOf(2000 - 64 + i))) {
                throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 2000 - 64 + i));
            }
            transaction.commit();
        }
        if (!tile.itemCount().equals(BigInteger.valueOf(2400 - 64))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 2400 - 64));
        }

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
        if (!tile.itemCount().equals(BigInteger.valueOf(2000 - 64))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 2000 - 64));
        }
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
        if (!tile.itemCount().equals(BigInteger.valueOf(300 - 64))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 300 - 64));
        }
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
            if (!tile.itemCount().equals(BigInteger.valueOf(initial - 64 - i))) {
                throw new GameTestAssertException("Violation of extracted count. " + tile.itemCount());
            }
            transaction.abort();
        }
        if (!tile.itemCount().equals(BigInteger.valueOf(initial - 64))) {
            throw new GameTestAssertException("Abort failed. Actual %s, Expected: %s".formatted(tile.itemCount(), initial - 64));
        }
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 100;
            var extracted = storage.extract(ItemVariant.of(item), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException("Extraction failed. %d, expected: %d".formatted(extracted, i));
            }
            transaction.commit();
        }
        if (!tile.itemCount().equals(BigInteger.valueOf(initial - 64 - 100))) {
            throw new GameTestAssertException("Violation of extracted count. " + tile.itemCount());
        }

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
        if (!tile.getHolding().isEmpty()) {
            throw new GameTestAssertException("Holding must be empty. " + tile.getHolding());
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
        if (!tile.itemCount().equals(BigInteger.valueOf(2000))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 2000));
        }

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
        if (!tile.getHolding().isEmpty()) {
            throw new GameTestAssertException("Holding must be empty. " + tile.getHolding());
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
        var secondItem = Items.APPLE;
        tile.setItem(1, new ItemStack(secondItem, 64));
        tile.setChanged();
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
        if (tile.getHolding().isEmpty()) {
            throw new GameTestAssertException("Holding must not be empty. " + tile.getHolding());
        }
        if (!tile.itemCount().equals(BigInteger.valueOf(32))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 32));
        }
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(secondItem, 64))) {
            throw new GameTestAssertException("Output slot. " + tile.getItem(1));
        }
        try (Transaction transaction = Transaction.openOuter()) {
            var i = 40;
            var extracted = storage.extract(ItemVariant.of(secondItem), i, transaction);
            if (extracted != i) {
                throw new GameTestAssertException("Extraction failed. %d, expected: %d".formatted(extracted, i));
            }
            transaction.commit();
            tile.setChanged();
        }
        if (!ItemStack.matches(tile.getItem(1), new ItemStack(secondItem, 24))) {
            throw new GameTestAssertException("Extracted Output slot. " + tile.getItem(1));
        }
        helper.succeed();
    }
}
