package com.kotori316.infchest.neoforge.tiles;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
final class InfItemHandlerTest {
    @Test
    void dummy() {
        assertInstanceOf(Class.class, InfItemHandler.class);
    }

    InfItemHandler createHandler() {
        return new InfItemHandler(createTile());
    }

    TileInfChestNeoForge createTile() {
        return assertDoesNotThrow(() -> new TileInfChestNeoForge(BlockPos.ZERO, InfChest.accessor.CHEST().defaultBlockState()));
    }

    @Test
    void handler() {
        assertNotNull(createHandler());
    }

    @Nested
    class Insert {
        @Test
        void actual(MinecraftServer ignore) {
            var handler = createHandler();
            assertEquals(BigInteger.ZERO, handler.infChest().totalCount());
            try (var tx = Transaction.openRoot()) {
                var inserted = handler.insert(0, ItemResource.of(Items.APPLE), 20, tx);
                assertEquals(20, inserted);
                tx.commit();
                assertEquals(BigInteger.valueOf(20), handler.infChest().totalCount());
            }
            assertEquals(BigInteger.valueOf(20), handler.infChest().totalCount());
        }

        @Test
        void aborted(MinecraftServer ignore) {
            var handler = createHandler();
            assertEquals(BigInteger.ZERO, handler.infChest().totalCount());
            try (var tx = Transaction.openRoot()) {
                var inserted = handler.insert(0, ItemResource.of(Items.APPLE), 20, tx);
                assertEquals(20, inserted);
                assertEquals(BigInteger.valueOf(20), handler.infChest().totalCount());
            }
            assertEquals(BigInteger.ZERO, handler.infChest().totalCount(), "The content must be rolled back");
        }

        @Test
        void viaHandler(MinecraftServer ignore) {
            var tile = createTile();
            var item = Items.CRIMSON_PLANKS;
            tile.addStack(new ItemStack(item), BigInteger.valueOf(2000));
            tile.setChanged();
            assertEquals(BigInteger.valueOf(2000), tile.totalCount());

            var handler = new InfItemHandler(tile);

            // Simulation
            try (var transaction = Transaction.openRoot()) {
                var i = 200;
                var inserted = handler.insert(0, ItemResource.of(item), i, transaction);
                assertEquals(i, inserted, "Insertion failed");
                assertEquals(BigInteger.valueOf(2000 + i), tile.totalCount());
                // transaction automatically aborts when not committed
            }
            assertEquals(BigInteger.valueOf(2000), tile.totalCount());

            // Execution
            var i = 400;
            try (var transaction = Transaction.openRoot()) {
                var inserted = handler.insert(0, ItemResource.of(item), i, transaction);
                assertEquals(i, inserted, "Insertion failed");
                assertEquals(BigInteger.valueOf(2000 + i), tile.totalCount());
                transaction.commit();
            }
            assertEquals(BigInteger.valueOf(2000 + i), tile.totalCount());
        }

        @Test
        void invalidItem(MinecraftServer ignore) {
            var tile = createTile();
            var item = Items.CRIMSON_PLANKS;
            tile.addStack(new ItemStack(item), BigInteger.valueOf(2000));
            tile.setChanged();
            var handler = new InfItemHandler(tile);

            try (var transaction = Transaction.openRoot()) {
                var inserted = handler.insert(0, ItemResource.of(Items.APPLE), 10, transaction);
                assertEquals(0, inserted, "Invalid items were inserted");
                // abort
            }
            assertEquals(BigInteger.valueOf(2000), tile.totalCount());
        }

        @Test
        void toEmpty(MinecraftServer ignore) {
            var tile = createTile();
            var handler = new InfItemHandler(tile);

            // Trial 1
            try (var transaction = Transaction.openRoot()) {
                var inserted = handler.insert(0, ItemResource.of(Items.APPLE), 300, transaction);
                assertEquals(300, inserted, "1: Insertion failed");
                assertTrue(ItemStack.isSameItemSameComponents(new ItemStack(Items.APPLE), tile.getHolding()), "1: Invalid items were inserted");
                // transaction automatically aborts when not committed
            }
            assertTrue(tile.getHolding().isEmpty() || tile.isEmpty(), "1: Abort failed");

            // Trial 2
            try (var transaction = Transaction.openRoot()) {
                var inserted = handler.insert(0, ItemResource.of(Items.APPLE), 300, transaction);
                tile.setChanged();
                assertEquals(300, inserted, "2: Insertion failed");
                assertTrue(ItemStack.isSameItemSameComponents(new ItemStack(Items.APPLE), tile.getHolding()), "2: Invalid items were inserted");
                // transaction automatically aborts when not committed
            }
            assertTrue(tile.getHolding().isEmpty() || tile.isEmpty(), "2: Abort failed");

            try (var transaction = Transaction.openRoot()) {
                handler.insert(0, ItemResource.of(Items.APPLE), 300, transaction);
                transaction.commit();
            }
            tile.setChanged();
            assertEquals(BigInteger.valueOf(300), tile.totalCount());
        }
    }

    @Nested
    class Extract {
        @Test
        void fromEmpty(MinecraftServer ignore) {
            var handler = createHandler();

            try (var transaction = Transaction.openRoot()) {
                var extracted = handler.extract(1, ItemResource.of(Items.APPLE), 10, transaction);
                assertEquals(0, extracted, "What item did you extracted?");
            }
        }

        @Test
        void viaHandler(MinecraftServer ignore) {
            var tile = createTile();
            var item = Items.CRIMSON_PLANKS;
            var initial = 2000;
            tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
            tile.setChanged();
            var handler = new InfItemHandler(tile);

            try (var transaction = Transaction.openRoot()) {
                var i = 10;
                var extracted = handler.extract(1, ItemResource.of(item), i, transaction);
                assertEquals(i, extracted, "Extraction failed");
                assertEquals(BigInteger.valueOf(initial - i), tile.totalCount());
                // transaction automatically aborts when not committed
            }
            assertEquals(BigInteger.valueOf(initial), tile.totalCount());

            try (var transaction = Transaction.openRoot()) {
                var i = 100;
                var extracted = handler.extract(1, ItemResource.of(item), i, transaction);
                assertEquals(i, extracted, "Extraction failed");
                assertEquals(BigInteger.valueOf(initial - i), tile.totalCount());
                transaction.commit();
            }
            assertEquals(BigInteger.valueOf(initial - 100), tile.totalCount());
        }

        @Test
        void viaHandler2(MinecraftServer ignore) {
            var tile = createTile();
            var item = Items.CRIMSON_PLANKS;
            var initial = 2064;
            tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
            tile.setChanged();
            var handler = new InfItemHandler(tile);

            try (var transaction = Transaction.openRoot()) {
                var i = 2000;
                var extracted = handler.extract(1, ItemResource.of(item), i, transaction);
                assertEquals(i, extracted, "Extraction failed");
                transaction.commit();
                tile.setChanged();
            }
            assertTrue(tile.getHolding().is(item), "Holding must be valid item");
            assertTrue(ItemStack.matches(tile.getItem(1), new ItemStack(item, 64)), "Output slot mismatch");
        }

        @Test
        void viaHandler3(MinecraftServer ignore) {
            var tile = createTile();
            var item = Items.CRIMSON_PLANKS;
            var initial = 2064;
            tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
            tile.setChanged();
            assertEquals(BigInteger.valueOf(initial), tile.totalCount());

            var handler = new InfItemHandler(tile);

            try (var transaction = Transaction.openRoot()) {
                var i = 2032;
                var extracted = handler.extract(1, ItemResource.of(item), i, transaction);
                assertEquals(i, extracted, "Extraction failed");
                transaction.commit();
                tile.setChanged();
            }
            assertTrue(tile.getHolding().is(item), "Holding must be valid item");
            assertTrue(ItemStack.matches(tile.getItem(1), new ItemStack(item, 32)), "Output slot mismatch");
        }

        @Test
        void viaHandler4(MinecraftServer ignore) {
            var tile = createTile();
            var item = Items.CRIMSON_PLANKS;
            var initial = 2064;
            tile.addStack(new ItemStack(item), BigInteger.valueOf(initial));
            tile.setChanged();
            var handler = new InfItemHandler(tile);

            try (var transaction = Transaction.openRoot()) {
                var i = 2032;
                var extracted = handler.extract(1, ItemResource.of(item), i, transaction);
                assertEquals(i, extracted, "Extraction failed");
                assertEquals(BigInteger.valueOf(initial - i), tile.totalCount());
                transaction.commit();
                tile.setChanged();
            }
            assertFalse(tile.getHolding().isEmpty(), "Holding must not be empty");
            assertEquals(BigInteger.valueOf(32), tile.totalCount());
            assertTrue(ItemStack.matches(tile.getItem(1), new ItemStack(item, 32)), "Output slot mismatch");

            try (var transaction = Transaction.openRoot()) {
                var i = 40;
                var extracted = handler.extract(1, ItemResource.of(item), i, transaction);
                assertEquals(32, extracted, "Extraction failed - should extract remaining 32 items");
                transaction.commit();
                tile.setChanged();
            }
            assertEquals(BigInteger.valueOf(0), tile.totalCount());
            assertTrue(tile.getHolding().isEmpty(), "Tile must be empty after extracting");
            assertTrue(tile.isEmpty(), "Tile must be empty after extracting");
        }
    }
}
