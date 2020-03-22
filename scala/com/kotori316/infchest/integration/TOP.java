package com.kotori316.infchest.integration;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

import com.kotori316.infchest.InfChest;

public class TOP {
    private static final String TOP_MODID = "theoneprobe";

    public static void register() {
        if (ModList.get().isLoaded(TOP_MODID)) {
            InterModComms.sendTo(InfChest.modID, TOP_MODID, "getTheOneProbe", TOPFunction::new);
        }
    }
}
