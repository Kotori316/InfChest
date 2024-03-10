package com.kotori316.infchest.neoforge.packets;

import com.kotori316.infchest.common.InfChest;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

public class PacketHandler {
    public static final String PROTOCOL = "1";

    public static void init(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar(InfChest.modID).versioned(PROTOCOL);
        registrar.play(
            ItemCountMessage.NAME,
            ItemCountMessage::new,
            handler -> handler.client(ItemCountMessage::onReceive)
        );
    }

    public static void sendToPoint(ItemCountMessage message) {
        PacketDistributor.NEAR.with(new PacketDistributor.TargetPoint(message.pos().getX(), message.pos().getY(), message.pos().getZ(), 16, message.dim()))
            .send(message);
    }
}
