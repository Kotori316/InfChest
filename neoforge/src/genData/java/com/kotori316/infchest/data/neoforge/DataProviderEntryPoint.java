package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
public final class DataProviderEntryPoint {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        InfChest.LOGGER.info("Start Data provider");
        event.getGenerator().addProvider(event.includeServer(), new RecipeNeoForge(event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }
}
