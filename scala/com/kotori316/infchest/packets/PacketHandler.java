package com.kotori316.infchest.packets;

import java.util.function.Predicate;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import com.kotori316.infchest.InfChest;

public class PacketHandler {
    public static final String PROTOCOL = "1";
    private static final SimpleChannel WRAPPER = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(InfChest.modID, "main"))
        .networkProtocolVersion(() -> PROTOCOL)
        .clientAcceptedVersions(Predicate.isEqual(PROTOCOL))
        .serverAcceptedVersions(Predicate.isEqual(PROTOCOL))
        .simpleChannel();

    public static void init() {
        WRAPPER.registerMessage(0, ItemCountMessage.class, ItemCountMessage::toBytes, ItemCountMessage::fromBytes, ItemCountMessage::onReceive);
//        WRAPPER.registerMessage(ItemCountMessage::onReceive, ItemCountMessage.class, 0, Side.CLIENT);
    }

    public static void sendToPoint(ItemCountMessage message) {
        WRAPPER.send(PacketDistributor.NEAR.with(() ->
                new PacketDistributor.TargetPoint(message.pos.getX(), message.pos.getY(), message.pos.getZ(), 16, DimensionType.getById(message.dim))),
            message);
    }
}
