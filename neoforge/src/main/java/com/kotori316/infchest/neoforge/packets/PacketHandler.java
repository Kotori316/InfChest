package com.kotori316.infchest.neoforge.packets;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.function.Predicate;

public class PacketHandler {
    public static final String PROTOCOL = "1";
    private static final SimpleChannel WRAPPER = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(InfChest.modID, "main"))
        .networkProtocolVersion(() -> PROTOCOL)
        .clientAcceptedVersions(Predicate.isEqual(PROTOCOL))
        .serverAcceptedVersions(Predicate.isEqual(PROTOCOL))
        .simpleChannel();

    public static void init() {
        WRAPPER.registerMessage(0, ItemCountMessage.class, ItemCountMessage::toBytes, ItemCountMessage::new, ItemCountMessage::onReceive);
    }

    public static void sendToPoint(ItemCountMessage message) {
        WRAPPER.send(PacketDistributor.NEAR.with(() ->
                new PacketDistributor.TargetPoint(message.pos().getX(), message.pos().getY(), message.pos().getZ(), 16, message.dim())),
            message);
    }
}
