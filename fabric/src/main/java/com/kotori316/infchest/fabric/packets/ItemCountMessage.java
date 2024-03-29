package com.kotori316.infchest.fabric.packets;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.math.BigInteger;
import java.util.Optional;

/**
 * To Client Only
 */
public record ItemCountMessage(BlockPos pos, ResourceKey<Level> dim, byte[] bytes, ItemStack out, ItemStack holding) {
    public static final ResourceLocation NAME = new ResourceLocation(InfChest.modID, "item_count_message");

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        this(
            chest.getBlockPos(),
            Optional.ofNullable(chest.getLevel()).map(Level::dimension).orElse(Level.OVERWORLD),
            integer.toByteArray(),
            chest.getItem(1),
            chest.getHoldingWithOneCount()
        );
    }

    public ItemCountMessage(FriendlyByteBuf p) {
        this(
            p.readBlockPos(),
            ResourceKey.create(Registries.DIMENSION, p.readResourceLocation()),
            p.readByteArray(),
            p.readItem(),
            p.readItem()
        );
    }

    public void toBytes(FriendlyByteBuf p) {
        p.writeBlockPos(pos).writeResourceLocation(dim.location());
        p.writeByteArray(bytes).writeItem(out).writeItem(holding);
    }

    public ResourceLocation getIdentifier() {
        return NAME;
    }

    static class HandlerHolder {
        static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler1, buf, responseSender) -> {
            var message = new ItemCountMessage(buf);
            var world = client.level;
            if (world != null && world.dimension().equals(message.dim)) {
                client.execute(() -> {
                    if (world.getBlockEntity(message.pos) instanceof TileInfChest chest) {
                        chest.setCount(new BigInteger(message.bytes));
                        chest.setItem(1, message.out);
                        chest.setHolding(message.holding);
                    }
                });
            }
        };
    }
}
