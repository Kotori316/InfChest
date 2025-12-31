package com.kotori316.infchest.forge.packets;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.packets.ItemCountMessage;
import net.minecraft.resources.Identifier;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {
    public static final int PROTOCOL = 1;
    private static final SimpleChannel WRAPPER = ChannelBuilder.named(Identifier.fromNamespaceAndPath(InfChest.modID, "main"))
        .networkProtocolVersion(PROTOCOL)
        .acceptedVersions(Channel.VersionTest.exact(PROTOCOL))
        .simpleChannel()
        .play()
        .bidirectional()
        // ItemCountMessage
        .addMain(ItemCountMessage.class, ItemCountMessage.STREAM_CODEC, ItemCountMessageForge::onReceive)
        .build();

    public static void init() {
    }

    public static void sendToPoint(ItemCountMessage message) {
        WRAPPER.send(message, PacketDistributor.NEAR.with(
            new PacketDistributor.TargetPoint(message.pos().getX(), message.pos().getY(), message.pos().getZ(), 16, message.dim())));
    }
}
