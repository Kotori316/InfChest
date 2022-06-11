package com.kotori316.infchest.integration;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.tiles.TileInfChest;

@WailaPlugin(InfChest.modID)
public class InfChestJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        IWailaPlugin.super.register(registration);
        registration.registerBlockDataProvider(InfChestJadeProvider.INSTANCE, TileInfChest.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        IWailaPlugin.super.registerClient(registration);
        registration.registerBlockComponent(InfChestJadeProvider.INSTANCE, BlockInfChest.class);
    }
}
