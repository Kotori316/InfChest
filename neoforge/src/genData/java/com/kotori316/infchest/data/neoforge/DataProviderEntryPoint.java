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

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
public final class DataProviderEntryPoint {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        InfChest.LOGGER.info("Start Data provider");
        event.getGenerator().addProvider(event.includeServer(), new RecipeNeoForge(event.getGenerator().getPackOutput(), event.getLookupProvider()));

        // Common parts
        event.getGenerator().addProvider(event.includeClient(), new PackMetadataGenerator(event.getGenerator().getPackOutput())
            .add(PackMetadataSection.TYPE, new PackMetadataSection(Component.literal("Inf Chest"), DetectedVersion.BUILT_IN.getPackVersion(PackType.CLIENT_RESOURCES))));
        var lootTableProvider = new LootTableProvider(
            event.getGenerator().getPackOutput(),
            Set.of(),
            List.of(
                new LootTableProvider.SubProviderEntry(LootSubProvider::new, LootContextParamSets.BLOCK)
            ),
            event.getLookupProvider()
        );
        event.getGenerator().addProvider(event.includeClient(), lootTableProvider);
        event.getGenerator().addProvider(event.includeClient(), new StateAndModelProvider(event.getGenerator().getPackOutput(), event.getExistingFileHelper()));
    }
}
