package com.kotori316.infchest.forge.packets;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.packets.ItemCountMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class PacketHandler {
    public static final int PROTOCOL = 1;
    private static final SimpleChannel WRAPPER = ChannelBuilder.named(new ResourceLocation(InfChest.modID, "main"))
        .networkProtocolVersion(PROTOCOL)
        .acceptedVersions(Channel.VersionTest.exact(PROTOCOL))
        .simpleChannel()
        // ItemCountMessage
        .messageBuilder(ItemCountMessage.class)
        .decoder(ItemCountMessage.STREAM_CODEC::decode)
        .encoder((message, friendlyByteBuf) -> ItemCountMessage.STREAM_CODEC.encode(friendlyByteBuf, message))
        .consumerMainThread(ItemCountMessageForge::onReceive)
        .add();

    public static void init() {
    }

    public static void sendToPoint(ItemCountMessage message) {
        WRAPPER.send(message, PacketDistributor.NEAR.with(
            new PacketDistributor.TargetPoint(message.pos().getX(), message.pos().getY(), message.pos().getZ(), 16, message.dim())));
    }
}
