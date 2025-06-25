package com.kotori316.infchest.forge.integration;

import com.kotori316.infchest.common.InfChest;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

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
