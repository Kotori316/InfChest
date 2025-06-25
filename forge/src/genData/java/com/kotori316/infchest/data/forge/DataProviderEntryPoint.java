package com.kotori316.infchest.data.forge;

import com.kotori316.infchest.common.InfChest;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
public final class DataProviderEntryPoint {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        InfChest.LOGGER.info("Start Data provider");
        event.getGenerator().addProvider(event.includeServer(), new RecipeForge(event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }
}
