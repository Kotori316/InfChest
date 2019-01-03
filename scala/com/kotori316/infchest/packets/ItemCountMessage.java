package com.kotori316.infchest.packets;

import java.math.BigInteger;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kotori316.infchest.tiles.TileInfChest;

/**
 * To Client Only
 */
public class ItemCountMessage implements IMessage {
    BlockPos pos;
    int dim;
    private byte[] bytes;

    @SuppressWarnings("unused")
    //Accessed via reflection
    public ItemCountMessage() {
    }

    public ItemCountMessage(TileInfChest chest, BigInteger integer) {
        pos = chest.getPos();
        dim = chest.getWorld().provider.getDimension();
        bytes = integer.toByteArray();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = BlockPos.fromLong(buf.readLong());
        dim = buf.readInt();
        int length = buf.readInt();
        bytes = new byte[length];
        buf.readBytes(bytes);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos.toLong()).writeInt(dim);
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
    }

    @SideOnly(Side.CLIENT)
    IMessage onReceive(MessageContext ctx) {
        TileEntity entity = Minecraft.getMinecraft().world.getTileEntity(pos);
        if (Minecraft.getMinecraft().world.provider.getDimension() == dim && entity instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) entity;
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> chest.setCount(new BigInteger(bytes)));
        }

        return null;
    }
}
