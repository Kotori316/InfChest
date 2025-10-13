package com.kotori316.infchest.neoforge.tiles;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

final class InfItemHandlerTest {
    @Test
    void dummy() {
        assertInstanceOf(Class.class, InfItemHandler.class);
    }

    InfItemHandler createHandler() {
        var tile = assertDoesNotThrow(() -> new TileInfChestNeoForge(BlockPos.ZERO, InfChest.accessor.CHEST().defaultBlockState()));
        return new InfItemHandler(tile);
    }

    @Test
    void handler() {
        assertNotNull(createHandler());
    }

    @Nested
    class Insert {
        @Test
        void actual() {
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
        void aborted() {
            var handler = createHandler();
            assertEquals(BigInteger.ZERO, handler.infChest().totalCount());
            try (var tx = Transaction.openRoot()) {
                var inserted = handler.insert(0, ItemResource.of(Items.APPLE), 20, tx);
                assertEquals(20, inserted);
                assertEquals(BigInteger.valueOf(20), handler.infChest().totalCount());
            }
            assertEquals(BigInteger.ZERO, handler.infChest().totalCount(), "The content must be rolled back");
        }
    }
}
