package com.kotori316.infchest.forge.packets;

import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.math.BigInteger;
import java.util.Optional;

/**
 * To Client Only
 */
public record ItemCountMessage(BlockPos pos, ResourceKey<Level> dim, byte[] bytes, ItemStack out, ItemStack holding) {

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

    void onReceive(CustomPayloadEvent.Context ctx) {
        assert Minecraft.getInstance().level != null;
        var entity = Minecraft.getInstance().level.getBlockEntity(pos);
        if (Minecraft.getInstance().level.dimension().equals(dim) && entity instanceof TileInfChest chest) {
            ctx.enqueueWork(() -> {
                chest.setCount(new BigInteger(bytes));
                chest.setItem(1, out);
                chest.setHolding(holding);
            });
            ctx.setPacketHandled(true);
        }
    }
}
