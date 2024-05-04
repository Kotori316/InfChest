package com.kotori316.infchest.forge.integration;

import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;

// @WailaPlugin(InfChest.modID) // wrong parchment mapping?
public class InfChestJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        IWailaPlugin.super.register(registration);
        registration.registerBlockDataProvider(new InfChestJadeProvider(), TileInfChest.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        IWailaPlugin.super.registerClient(registration);
        registration.registerBlockComponent(new InfChestJadeProvider(), BlockInfChest.class);
    }
}
