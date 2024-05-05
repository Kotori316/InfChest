package com.kotori316.infchest.fabric.packets;

import com.kotori316.infchest.common.packets.ItemCountMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PacketHandler {
    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            ClientPlayNetworking.registerGlobalReceiver(ItemCountMessage.TYPE, ItemCountMessageFabric.HandlerHolder.HANDLER);
        }
    }

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ItemCountMessage.TYPE, ItemCountMessage.STREAM_CODEC);
    }

    public static void sendToClientPlayer(@NotNull ItemCountMessage message, @NotNull ServerPlayer player) {
        ServerPlayNetworking.send(player, message);
    }
}
