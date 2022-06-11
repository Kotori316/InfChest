package com.kotori316.infchest;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

import com.kotori316.infchest.guis.GuiInfChest;
import com.kotori316.infchest.packets.PacketHandler;

public class InfChestClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PacketHandler.Client.initClient();
        MenuScreens.register(InfChest.Register.INF_CHEST_CONTAINER_TYPE, GuiInfChest::new);
    }
}
