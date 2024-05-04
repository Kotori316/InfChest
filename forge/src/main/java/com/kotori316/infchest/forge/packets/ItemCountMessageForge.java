package com.kotori316.infchest.forge.packets;

import com.kotori316.infchest.common.packets.ItemCountMessage;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.network.CustomPayloadEvent;

public final class ItemCountMessageForge {
    static void onReceive(ItemCountMessage message, CustomPayloadEvent.Context ctx) {
        assert Minecraft.getInstance().level != null;
        message.onReceive(Minecraft.getInstance().level);
    }
}
