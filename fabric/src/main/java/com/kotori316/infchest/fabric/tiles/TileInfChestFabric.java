package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.packets.ItemCountMessage;
import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.fabric.packets.PacketHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public final class TileInfChestFabric extends TileInfChest implements ExtendedMenuProvider<BlockPos> {
    public TileInfChestFabric(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    private record MessageSender(ServerPlayer player, TileInfChest chest) implements Runnable {
        @Override
        public void run() {
            PacketHandler.sendToClientPlayer(new ItemCountMessage(chest, chest.totalCount()), player);
        }

        boolean playerEqual(Player player) {
            return this.player.getGameProfile().id().equals(player.getGameProfile().id());
        }
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (level != null && !level.isClientSide() && user instanceof ServerPlayer player) {
            var messageSender = new MessageSender(player, this);
            addUpdate(messageSender);
        }
        super.startOpen(user);
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (level != null && !level.isClientSide() && user instanceof ServerPlayer player) {
            runUpdateRemoveIf(r -> (r instanceof MessageSender m) && m.playerEqual(player));
        }
        super.stopOpen(user);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return worldPosition;
    }
}
