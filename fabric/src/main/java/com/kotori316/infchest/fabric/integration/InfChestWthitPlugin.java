package com.kotori316.infchest.fabric.integration;

import com.kotori316.infchest.common.tiles.TileInfChest;
import mcp.mobius.waila.api.ICommonRegistrar;
import mcp.mobius.waila.api.IWailaCommonPlugin;

public final class InfChestWthitPlugin implements IWailaCommonPlugin {
    @Override
    public void register(ICommonRegistrar registrar) {
        InfChestWthitProvider provider = new InfChestWthitProvider();
        registrar.blockData(provider, TileInfChest.class);
    }
}
