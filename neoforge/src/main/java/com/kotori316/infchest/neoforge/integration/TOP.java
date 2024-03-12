package com.kotori316.infchest.neoforge.integration;

import com.kotori316.infchest.common.InfChest;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = InfChest.modID)
public class TOP {
    private static final String TOP_MODID = "theoneprobe";

    @SubscribeEvent
    public static void register(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded(TOP_MODID)) {
            InterModComms.sendTo(InfChest.modID, TOP_MODID, "getTheOneProbe", TOPFunction::new);
        }
    }
}
