package com.kotori316.infchest.packets;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import com.kotori316.infchest.tiles.TileInfChest;

/**
 * To Client Only
 */
public class ItemCountMessage {
    BlockPos pos;
    RegistryKey<World> dim;
    private byte[] bytes;
    private ItemStack out, holding;

    @SuppressWarnings("unused")
    //Accessed via reflection
    public ItemCountMessage() {
    }

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        pos = chest.getPos();
        dim = Optional.ofNullable(chest.getWorld()).map(World::func_234923_W_).orElse(World.field_234918_g_);
        bytes = integer.toByteArray();
        out = chest.getStackInSlot(1);
        holding = chest.getStack(1);
    }

    public static ItemCountMessage fromBytes(PacketBuffer p) {
        ItemCountMessage message = new ItemCountMessage();
        message.pos = p.readBlockPos();
        message.dim = RegistryKey.func_240903_a_(Registry.WORLD_KEY, p.readResourceLocation());
        message.bytes = p.readByteArray();
        message.out = p.readItemStack();
        message.holding = p.readItemStack();
        return message;
    }

    public void toBytes(PacketBuffer p) {
        p.writeBlockPos(pos).writeResourceLocation(dim.func_240901_a_());
        p.writeByteArray(bytes).writeItemStack(out).writeItemStack(holding);
    }

    void onReceive(Supplier<NetworkEvent.Context> ctx) {
        assert Minecraft.getInstance().world != null;
        TileEntity entity = Minecraft.getInstance().world.getTileEntity(pos);
        if (Minecraft.getInstance().world.func_234923_W_().equals(dim) && entity instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) entity;
            ctx.get().enqueueWork(() -> {
                chest.setCount(new BigInteger(bytes));
                chest.setInventorySlotContents(1, out);
                chest.setHolding(holding);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
