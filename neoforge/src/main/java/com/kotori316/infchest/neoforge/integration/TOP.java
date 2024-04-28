package com.kotori316.infchest.neoforge.integration;

import com.kotori316.infchest.common.InfChest;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
public class TOP {
    private static final String TOP_MOD_ID = "theoneprobe";

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded(TOP_MOD_ID)) {
            InterModComms.sendTo(InfChest.modID, TOP_MOD_ID, "getTheOneProbe", TOPFunction::new);
        }
    }
}
