package com.kotori316.infchest.neoforge.packets;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.math.BigInteger;
import java.util.Optional;

/**
 * To Client Only
 */
public record ItemCountMessage(BlockPos pos, ResourceKey<Level> dim, byte[] bytes, ItemStack out,
                               ItemStack holding) implements CustomPacketPayload {
    public static final ResourceLocation NAME = new ResourceLocation(InfChest.modID, "item_count_message");

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        this(
            chest.getBlockPos(),
            Optional.ofNullable(chest.getLevel()).map(Level::dimension).orElse(Level.OVERWORLD),
            integer.toByteArray(),
            chest.getItem(1),
            chest.getStack(1)
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

    @Override
    public void write(FriendlyByteBuf p) {
        p.writeBlockPos(pos).writeResourceLocation(dim.location());
        p.writeByteArray(bytes).writeItem(out).writeItem(holding);
    }

    @Override
    public ResourceLocation id() {
        return NAME;
    }

    void onReceive(PlayPayloadContext context) {
        assert Minecraft.getInstance().level != null;
        var entity = Minecraft.getInstance().level.getBlockEntity(pos);
        if (Minecraft.getInstance().level.dimension().equals(dim) && entity instanceof TileInfChest chest) {
            context.workHandler().execute(() -> {
                chest.setCount(new BigInteger(bytes));
                chest.setItem(1, out);
                chest.setHolding(holding);
            });
        }
    }
}
