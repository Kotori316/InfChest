package com.kotori316.infchest.fabric.integration;

import com.kotori316.infchest.common.tiles.TileInfChest;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IWailaClientPlugin;

public final class InfChestWthitClientPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        InfChestWthitProvider provider = new InfChestWthitProvider();
        registrar.body(provider, TileInfChest.class);
    }
}
