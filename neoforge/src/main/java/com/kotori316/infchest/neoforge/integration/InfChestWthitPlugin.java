package com.kotori316.infchest.neoforge.integration;

import com.kotori316.infchest.common.tiles.TileInfChest;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;

@SuppressWarnings("unused")
public final class InfChestWthitPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        InfChestWthitProvider provider = new InfChestWthitProvider();
        registrar.addComponent(provider, TooltipPosition.BODY, TileInfChest.class);
        registrar.addBlockData(provider, TileInfChest.class);
    }
}
