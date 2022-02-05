package com.kotori316.infchest.packets;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class PacketHandler {
    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            var list = List.of(
                new ClientPacketInit(ItemCountMessage.NAME, ItemCountMessage.HandlerHolder.HANDLER)
            );
            list.forEach(i -> ClientPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
        }

        private record ClientPacketInit(Identifier name, ClientPlayNetworking.PlayChannelHandler handler) {
        }
    }

    public static void sendToClientPlayer(@NotNull ItemCountMessage message, @NotNull ServerPlayerEntity player) {
        var packet = PacketByteBufs.create();
        message.toBytes(packet);
        ServerPlayNetworking.send(player, message.getIdentifier(), packet);
    }
}
