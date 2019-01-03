package com.kotori316.infchest.packets;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import com.kotori316.infchest.InfChest;

public class PacketHandler {
    private static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(InfChest.MOD_NAME);

    public static void init() {
        WRAPPER.registerMessage(ItemCountMessage::onReceive, ItemCountMessage.class, 0, Side.CLIENT);
    }

    public static void sendToPoint(ItemCountMessage message) {
        WRAPPER.sendToAllAround(message, new NetworkRegistry.TargetPoint(message.dim, message.pos.getX(), message.pos.getY(), message.pos.getZ(), 16));
    }
}
