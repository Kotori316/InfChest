package com.kotori316.infchest.common.tiles;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueOutput;

public final class TileUtil {
    public static TagValueOutput saveWithoutMetadata(BlockEntity entity) {
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, providerFromEntity(entity));
        entity.saveWithoutMetadata(output);
        return output;
    }

    public static HolderLookup.Provider providerFromEntity(BlockEntity entity) {
        var level = entity.getLevel();
        if (level == null) {
            throw new IllegalStateException("Level in entity cannot be null!");
        }
        return level.registryAccess();
    }

    public static ItemStack parseItemStack(BlockEntity entity, CompoundTag tag) {
        var ops = providerFromEntity(entity).createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.parse(ops, tag)
            .result()
            .orElse(ItemStack.EMPTY);
    }

    public static Tag serializeItemStack(BlockEntity entity, ItemStack stack) {
        var ops = providerFromEntity(entity).createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack)
            .result()
            .orElseThrow(() -> new IllegalStateException("Failed to serialize ItemStack: " + stack));
    }
}
