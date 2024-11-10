package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.blocks.ContentInfChest;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;

import java.util.List;
import java.util.Set;

public final class LootSubProvider extends BlockLootSubProvider {
    LootSubProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS, provider);
    }

    @Override
    protected void generate() {
        add(InfChest.accessor.CHEST(), b -> this.createSingleItemTable(b).apply(ContentInfChest.builder()));
        add(InfChest.accessor.DEQUE(), b -> this.createSingleItemTable(b).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(
            InfChest.accessor.CHEST(),
            InfChest.accessor.DEQUE()
        );
    }
}
