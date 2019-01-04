package com.kotori316.infchest.integration;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.kotori316.infchest.InfChest;

@Mod(modid = AE2.modID, name = AE2.Mod_Name, version = "${version}", certificateFingerprint = "@FINGERPRINT@",
    dependencies = "required-after:infchest;")
public class AE2 {
    public static final String modID = "infchest_ae2";
    public static final String Mod_Name = "InfChest_AE2";
    public static final String AE2_modID = "appliedenergistics2";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata modMetadata = event.getModMetadata();
        modMetadata.parent = InfChest.modID;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (Loader.isModLoaded(AE2_modID)) {
            try {
                MinecraftForge.EVENT_BUS.register(Class.forName("com.kotori316.infchest.integration.AE2Capability"));
            } catch (ClassNotFoundException e) {
                InfChest.LOGGER.error("AE2Capability not found.", e);
            }
        }
    }
}
