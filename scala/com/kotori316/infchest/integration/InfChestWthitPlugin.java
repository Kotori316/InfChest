package com.kotori316.infchest.integration;

import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;

import com.kotori316.infchest.tiles.TileInfChest;

@SuppressWarnings("unused")
public class InfChestWthitPlugin implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        InfChestWthitProvider provider = new InfChestWthitProvider();
        registrar.addComponent(provider, TooltipPosition.BODY, TileInfChest.class);
        registrar.addBlockData(provider, TileInfChest.class);
    }
}
