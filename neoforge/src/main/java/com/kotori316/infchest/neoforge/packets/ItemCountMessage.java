package com.kotori316.infchest.neoforge.packets;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.math.BigInteger;
import java.util.Optional;

/**
 * To Client Only
 */
public record ItemCountMessage(BlockPos pos, ResourceKey<Level> dim, byte[] bytes, ItemStack out,
                               ItemStack holding) implements CustomPacketPayload {
    public static final ResourceLocation NAME = new ResourceLocation(InfChest.modID, "item_count_message");
    static final CustomPacketPayload.Type<ItemCountMessage> TYPE = new Type<>(NAME);
    static final StreamCodec<RegistryFriendlyByteBuf, ItemCountMessage> STREAM_CODEC = CustomPacketPayload.codec(
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
    public CustomPacketPayload.Type<ItemCountMessage> type() {
        return TYPE;
    }

    void onReceive(IPayloadContext context) {
        assert Minecraft.getInstance().level != null;
        var entity = Minecraft.getInstance().level.getBlockEntity(pos);
        if (Minecraft.getInstance().level.dimension().equals(dim) && entity instanceof TileInfChest chest) {
            context.enqueueWork(() -> {
                chest.setCount(new BigInteger(bytes));
                chest.setItem(1, out);
                chest.setHolding(holding);
            });
        }
    }
}
