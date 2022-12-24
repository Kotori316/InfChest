package com.kotori316.infchest.fabric.tiles;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.fabric.packets.ItemCountMessage;
import com.kotori316.infchest.fabric.packets.PacketHandler;

public final class TileInfChestFabric extends TileInfChest implements ExtendedScreenHandlerFactory {
    public TileInfChestFabric(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private record MessageSender(ServerPlayer player, TileInfChest chest) implements Runnable {
        @Override
        public void run() {
            PacketHandler.sendToClientPlayer(new ItemCountMessage(chest, chest.itemCount()), player);
        }

        boolean playerEqual(Player player) {
            return this.player.getGameProfile().getId().equals(player.getGameProfile().getId());
        }
    }

    ItemStack getHolding() {
        return this.holding;
    }

    @Override
    public void startOpen(Player player) {
        if (level != null && !level.isClientSide && player instanceof ServerPlayer) {
            var messageSender = new MessageSender((ServerPlayer) player, this);
            addUpdate(messageSender);
        }
        super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        if (level != null && !level.isClientSide && player instanceof ServerPlayer) {
            getUpdates().removeIf(r -> (r instanceof MessageSender m) && m.playerEqual(player));
        }
        super.stopOpen(player);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(worldPosition);
    }
}
