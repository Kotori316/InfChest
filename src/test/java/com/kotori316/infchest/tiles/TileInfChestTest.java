package com.kotori316.infchest.tiles;

import java.math.BigInteger;
import java.util.Objects;
import java.util.stream.IntStream;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.kotori316.infchest.InfChest;

@SuppressWarnings("UnstableApiUsage")
public class TileInfChestTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void placeOne(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = helper.getBlockEntity(pos);
        if (tile instanceof TileInfChest) {
            helper.succeed();
        } else {
            throw new GameTestAssertException("Unknown tile exists, %s".formatted(tile));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void isEmpty(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) helper.getBlockEntity(pos);
        assert tile != null;
        if (tile.isEmpty()) {
            helper.succeed();
        } else {
            var t = IntStream.range(0, tile.getContainerSize()).mapToObj(tile::getItem).toList();
            throw new GameTestAssertException("Tile must be empty, %s".formatted(t));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void addItem1(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
        tile.setItem(0, new ItemStack(Items.APPLE, 4));
        tile.setItem(0, new ItemStack(Items.APPLE, 64));

        var stack = tile.getItem(1);
        if (ItemStack.matches(stack, new ItemStack(Items.APPLE, 64)) && tile.itemCount().equals(BigInteger.valueOf(4))) {
            helper.succeed();
        } else {
            throw new GameTestAssertException("Tile has unexpected item, %s, %s".formatted(stack, tile.itemCount()));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void addItem3(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
        var stack = new ItemStack(Items.APPLE, 64);
        tile.setItem(0, stack.copy());
        tile.setItem(0, stack.copy());
        tile.setItem(0, new ItemStack(Items.APPLE, 32));

        var removed = tile.removeItem(1, 64);
        if (!ItemStack.isSame(removed, stack)) {
            throw new GameTestAssertException("%s must be taken. %s".formatted(stack, removed));
        }
        tile.setChanged();
        var out = tile.getItem(1);
        if (!ItemStack.matches(out, stack)) {
            throw new GameTestAssertException("Out slot must be updated. %s".formatted(out));
        }
        if (!tile.itemCount().equals(BigInteger.valueOf(32))) {
            throw new GameTestAssertException("ItemCount, A: %s, E: %s".formatted(tile.itemCount(), 32));
        }

        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void insertViaStorage(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
        helper.setBlock(pos, InfChest.Register.CHEST);

        var tile = (TileInfChest) Objects.requireNonNull(helper.getBlockEntity(pos));
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
