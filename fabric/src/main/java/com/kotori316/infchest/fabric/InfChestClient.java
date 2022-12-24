package com.kotori316.infchest.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

import com.kotori316.infchest.common.guis.GuiInfChest;
import com.kotori316.infchest.fabric.packets.PacketHandler;

public class InfChestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PacketHandler.Client.initClient();
        MenuScreens.register(InfChestFabric.Register.INF_CHEST_CONTAINER_TYPE, GuiInfChest::new);
    }
}
