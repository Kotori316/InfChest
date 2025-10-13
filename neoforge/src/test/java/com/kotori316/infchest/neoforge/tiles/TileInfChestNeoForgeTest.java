package com.kotori316.infchest.neoforge.tiles;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
final class TileInfChestNeoForgeTest {
    @Test
    void dummy() throws ReflectiveOperationException {
        var clazz = Class.forName("com.kotori316.infchest.neoforge.tiles.TileInfChestNeoForge");
        assertTrue(BlockEntity.class.isAssignableFrom(clazz));
    }

    @Test
    void createTile() {
        var tile = assertDoesNotThrow(() -> new TileInfChestNeoForge(BlockPos.ZERO, InfChest.accessor.CHEST().defaultBlockState()));
        assertNotNull(tile);
    }
}
