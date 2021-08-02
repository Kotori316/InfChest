package com.kotori316.infchest.packets;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import com.kotori316.infchest.tiles.TileInfChest;

/**
 * To Client Only
 */
public class ItemCountMessage {
    BlockPos pos;
    ResourceKey<Level> dim;
    private byte[] bytes;
    private ItemStack out, holding;

    @SuppressWarnings("unused")
    //Accessed via reflection
    public ItemCountMessage() {
    }

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        pos = chest.getBlockPos();
        dim = Optional.ofNullable(chest.getLevel()).map(Level::dimension).orElse(Level.OVERWORLD);
        bytes = integer.toByteArray();
        out = chest.getItem(1);
        holding = chest.getStack(1);
    }

    public static ItemCountMessage fromBytes(FriendlyByteBuf p) {
        ItemCountMessage message = new ItemCountMessage();
        message.pos = p.readBlockPos();
        message.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, p.readResourceLocation());
        message.bytes = p.readByteArray();
        message.out = p.readItem();
        message.holding = p.readItem();
        return message;
    }

    public void toBytes(FriendlyByteBuf p) {
        p.writeBlockPos(pos).writeResourceLocation(dim.location());
        p.writeByteArray(bytes).writeItem(out).writeItem(holding);
    }

    void onReceive(Supplier<NetworkEvent.Context> ctx) {
        assert Minecraft.getInstance().level != null;
        var entity = Minecraft.getInstance().level.getBlockEntity(pos);
        if (Minecraft.getInstance().level.dimension().equals(dim) && entity instanceof TileInfChest chest) {
            ctx.get().enqueueWork(() -> {
                chest.setCount(new BigInteger(bytes));
                chest.setItem(1, out);
                chest.setHolding(holding);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
