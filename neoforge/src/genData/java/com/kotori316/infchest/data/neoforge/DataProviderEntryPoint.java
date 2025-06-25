package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.DetectedVersion;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = InfChest.modID)
public final class DataProviderEntryPoint {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Server event) {
        InfChest.LOGGER.info("Start Server Data provider");
        event.createProvider(RecipeNeoForge::new);
    }

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        InfChest.LOGGER.info("Start Client Data provider");
        // Common parts
        event.createProvider(PackMetadataGenerator::new)
            .add(PackMetadataSection.TYPE, new PackMetadataSection(Component.literal("Inf Chest"), DetectedVersion.BUILT_IN.packVersion(PackType.CLIENT_RESOURCES)));
        var lootTableProvider = new LootTableProvider(
            event.getGenerator().getPackOutput(),
            Set.of(),
            List.of(
                new LootTableProvider.SubProviderEntry(LootSubProvider::new, LootContextParamSets.BLOCK)
            ),
            event.getLookupProvider()
        );
        event.addProvider(lootTableProvider);
        event.createProvider(StateAndModelProvider::new);
        event.createProvider(LangProvider::new);
    }
}
