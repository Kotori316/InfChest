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
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = InfChest.modID)
public final class DataProviderEntryPoint {

    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        InfChest.LOGGER.info("Start Client Data provider");
        // Common parts
        event.createProvider(PackMetadataGenerator::new)
            .add(PackMetadataSection.CLIENT_TYPE, new PackMetadataSection(Component.literal("Inf Chest"), DetectedVersion.BUILT_IN.packVersion(PackType.CLIENT_RESOURCES).minorRange()));
        event.createProvider(StateAndModelProvider::new);
        event.createProvider(LangProvider::new);
    }

    @SubscribeEvent
    public static void gatherRegistryEntry(GatherDataRegistryEntriesEvent event) {
        var generationMode = System.getenv("DATA_GENERATION_MODE");
        InfChest.LOGGER.info("Start Registry Data provider for {}", generationMode);
        switch (generationMode) {
            case "server" -> event.add(new RecipeNeoForge());
            case "client" -> event.lootTable(
                new LootTableProvider.SubProviderEntry(LootSubProvider::new, LootContextParamSets.BLOCK)
            );
            case null, default -> throw new IllegalArgumentException("Unknown mode: %s".formatted(generationMode));
        }
    }
}
