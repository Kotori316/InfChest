package com.kotori316.infchest.integration;

import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

import com.kotori316.infchest.tiles.TileInfChest;

@WailaPlugin
public class Waila implements IWailaPlugin {
    static final String Waila_ModId = "waila";

    @Override
    public void register(IWailaRegistrar registrar) {
        WailaProvider provider = new WailaProvider();
        registrar.registerBodyProvider(provider, TileInfChest.class);
        registrar.registerNBTProvider(provider, TileInfChest.class);
    }
}
