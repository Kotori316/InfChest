package com.kotori316.infchest.data.forge;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraftforge.common.data.RegistryDataBuilder;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
public final class DataProviderEntryPoint {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        InfChest.LOGGER.info("Start Data provider");
        var builder = RegistryDataBuilder.of().modid(InfChest.modID);
        builder.reloadable(new RegistrySetBuilder().add(new RecipeForge()));
        var generator = event.getGenerator();
        generator.addProvider(event.includeServer(), builder.reloadableGenerator(generator.getPackOutput()));
    }
}
