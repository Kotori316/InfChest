package com.kotori316.infchest.integration;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.kotori316.infchest.InfChest;

@Mod(name = TOP.Mod_Name, modid = TOP.modID, version = "${version}", certificateFingerprint = "@FINGERPRINT@",
    dependencies = "required-after:infchest;")
public class TOP {
    public static final String modID = "infchest_top";
    static final String Mod_Name = "InfChest_TOP";
    private static final String TOP_MODID = "theoneprobe";


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata metadata = event.getModMetadata();
        metadata.parent = InfChest.modID;
        if (Loader.isModLoaded(TOP_MODID)) {
            final String topFunction = "TOPFunction";
            String packageName = TOPFunction.class.getPackage().getName();
            assert (packageName + "." + topFunction).equals(TOPFunction.class.getName());
            FMLInterModComms.sendFunctionMessage(TOP_MODID, "getTheOneProbe",
                packageName + "." + topFunction);
        }
    }
}
