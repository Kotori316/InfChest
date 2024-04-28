package com.kotori316.infchest.neoforge.packets;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.packets.ItemCountMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.jetbrains.annotations.Nullable;

public class PacketHandler {
    public static final String PROTOCOL = "1";

    public static void init(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(InfChest.modID).versioned(PROTOCOL);
        registrar.playToClient(
            ItemCountMessage.TYPE,
            ItemCountMessage.STREAM_CODEC,
            ItemCountMessageNeoForge::onReceive
        );
    }

    public static void sendToPoint(ItemCountMessage message, @Nullable Level level) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersInDimension(serverLevel, message);
        } else {
            InfChest.LOGGER.error("PacketHandler#sendToPoint is called in client level");
        }
    }
}
