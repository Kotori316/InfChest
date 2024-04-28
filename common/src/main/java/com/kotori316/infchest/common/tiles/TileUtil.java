package com.kotori316.infchest.common.tiles;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class TileUtil {
    public static CompoundTag saveWithoutMetadata(BlockEntity entity) {
        return entity.saveWithoutMetadata(providerFromEntity(entity));
    }

    public static HolderLookup.Provider providerFromEntity(BlockEntity entity) {
        var level = entity.getLevel();
        if (level == null) {
            throw new IllegalStateException("Level in entity cannot be null!");
        }
        return level.registryAccess();
    }
}
