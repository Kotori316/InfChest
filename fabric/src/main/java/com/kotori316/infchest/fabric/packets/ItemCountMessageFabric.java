package com.kotori316.infchest.fabric.packets;

import com.kotori316.infchest.common.packets.ItemCountMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

class ItemCountMessageFabric {
    static class HandlerHolder {
        static final ClientPlayNetworking.PlayPayloadHandler<ItemCountMessage> HANDLER = (message, context) -> {
            var level = context.client().level;
            context.client().execute(() -> message.onReceive(level));
        };
    }
}
