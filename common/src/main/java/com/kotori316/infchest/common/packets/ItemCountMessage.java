package com.kotori316.infchest.common.packets;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.math.BigInteger;
import java.util.Optional;

/**
 * To Client Only
 */
public record ItemCountMessage(BlockPos pos, ResourceKey<Level> dim, byte[] bytes, ItemStack out,
                               ItemStack holding) implements CustomPacketPayload {
    public static final ResourceLocation NAME = new ResourceLocation(InfChest.modID, "item_count_message");
    public static final Type<ItemCountMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemCountMessage> STREAM_CODEC = CustomPacketPayload.codec(
        ItemCountMessage::write, ItemCountMessage::new
    );

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        this(
            chest.getBlockPos(),
            Optional.ofNullable(chest.getLevel()).map(Level::dimension).orElse(Level.OVERWORLD),
            integer.toByteArray(),
            chest.getItem(1),
            chest.getHoldingWithOneCount()
        );
    }

    public ItemCountMessage(RegistryFriendlyByteBuf p) {
        this(
            p.readBlockPos(),
            ResourceKey.create(Registries.DIMENSION, p.readResourceLocation()),
            p.readByteArray(),
            p.readJsonWithCodec(ItemStack.OPTIONAL_CODEC),
            p.readJsonWithCodec(ItemStack.OPTIONAL_CODEC)
        );
    }

    void write(RegistryFriendlyByteBuf p) {
        p.writeBlockPos(pos).writeResourceLocation(dim.location());
        p.writeByteArray(bytes);
        p.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, out);
        p.writeJsonWithCodec(ItemStack.OPTIONAL_CODEC, holding);
    }

    @Override
    public Type<ItemCountMessage> type() {
        return TYPE;
    }

    public void onReceive(Level level) {
        var entity = level.getBlockEntity(this.pos());
        if (level.dimension().equals(this.dim()) && entity instanceof TileInfChest chest) {
            chest.setCount(new BigInteger(this.bytes()));
            chest.setItem(1, this.out());
            chest.setHolding(this.holding());
        }
    }
}
