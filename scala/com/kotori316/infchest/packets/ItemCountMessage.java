package com.kotori316.infchest.packets;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import com.kotori316.infchest.tiles.TileInfChest;

/**
 * To Client Only
 */
public class ItemCountMessage {
    BlockPos pos;
    int dim;
    private byte[] bytes;
    private ItemStack out;

    @SuppressWarnings("unused")
    //Accessed via reflection
    public ItemCountMessage() {
    }

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        pos = chest.getPos();
        dim = Optional.ofNullable(chest.getWorld()).map(World::getDimension).map(Dimension::getType).map(DimensionType::getId).orElse(0);
        bytes = integer.toByteArray();
        out = chest.getStackInSlot(1);
    }

    public static ItemCountMessage fromBytes(PacketBuffer p) {
        ItemCountMessage message = new ItemCountMessage();
        message.pos = p.readBlockPos();
        message.dim = p.readInt();
        message.bytes = p.readByteArray();
        message.out = p.readItemStack();
        return message;
    }

    public void toBytes(PacketBuffer p) {
        p.writeBlockPos(pos).writeInt(dim);
        p.writeByteArray(bytes).writeItemStack(out);
    }

    @OnlyIn(Dist.CLIENT)
    void onReceive(Supplier<NetworkEvent.Context> ctx) {
        TileEntity entity = Minecraft.getInstance().world.getTileEntity(pos);
        if (Minecraft.getInstance().world.getDimension().getType().getId() == dim && entity instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) entity;
            ctx.get().enqueueWork(() -> {
                chest.setCount(new BigInteger(bytes));
                chest.setInventorySlotContents(1, out);
            });
        }
    }
}
