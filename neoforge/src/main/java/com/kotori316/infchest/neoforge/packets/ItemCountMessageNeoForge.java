package com.kotori316.infchest.neoforge.packets;

import com.kotori316.infchest.common.packets.ItemCountMessage;
import net.neoforged.neoforge.network.handling.IPayloadContext;

class ItemCountMessageNeoForge {
    static void onReceive(ItemCountMessage message, IPayloadContext context) {
        context.enqueueWork(() -> message.onReceive(context.player().level()));
    }
}
