package com.kotori316.infchest.integration;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

@WailaPlugin(id = InfChest.modID + ":wthit_plugin")
public class Waila implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        WailaProvider provider = new WailaProvider();
        registrar.addComponent(provider, TooltipPosition.BODY, TileInfChest.class);
        registrar.addBlockData(provider, TileInfChest.class);
    }
}
