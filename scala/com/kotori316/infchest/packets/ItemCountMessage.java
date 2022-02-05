package com.kotori316.infchest.packets;

import java.math.BigInteger;
import java.util.Optional;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

/**
 * To Client Only
 */
public record ItemCountMessage(BlockPos pos, RegistryKey<World> dim, byte[] bytes, ItemStack out, ItemStack holding) {
    public static final Identifier NAME = new Identifier(InfChest.modID, "item_count_message");

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        this(
            chest.getPos(),
            Optional.ofNullable(chest.getWorld()).map(World::getRegistryKey).orElse(World.OVERWORLD),
            integer.toByteArray(),
            chest.getStack(1),
            chest.getStackWithAmount(1)
        );
    }

    public ItemCountMessage(PacketByteBuf p) {
        this(
            p.readBlockPos(),
            RegistryKey.of(Registry.WORLD_KEY, p.readIdentifier()),
            p.readByteArray(),
            p.readItemStack(),
            p.readItemStack()
        );
    }

    public void toBytes(PacketByteBuf p) {
        p.writeBlockPos(pos).writeIdentifier(dim.getValue());
        p.writeByteArray(bytes).writeItemStack(out).writeItemStack(holding);
    }

    public Identifier getIdentifier() {
        return NAME;
    }

    static class HandlerHolder {
        static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler1, buf, responseSender) -> {
            var message = new ItemCountMessage(buf);
            var world = client.world;
            if (world != null && world.getRegistryKey().equals(message.dim)) {
                client.execute(() -> {
                    if (world.getBlockEntity(message.pos) instanceof TileInfChest chest) {
                        chest.setCount(new BigInteger(message.bytes));
                        chest.setStack(1, message.out);
                        chest.setHolding(message.holding);
                    }
                });
            }
        };
    }
}
