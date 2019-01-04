package com.kotori316.infchest.packets;

import java.io.IOException;
import java.math.BigInteger;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

/**
 * To Client Only
 */
public class ItemCountMessage implements IMessage {
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
        dim = chest.getWorld().provider.getDimension();
        bytes = integer.toByteArray();
        out = chest.getStackInSlot(1);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        pos = p.readBlockPos();
        dim = p.readInt();
        bytes = p.readByteArray();
        try {
            out = p.readItemStack();
        } catch (IOException e) {
            InfChest.LOGGER.error("ItemStack reading.", e);
            out = ItemStack.EMPTY;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer p = new PacketBuffer(buf);
        p.writeBlockPos(pos).writeInt(dim);
        p.writeByteArray(bytes).writeItemStack(out);
    }

    @SideOnly(Side.CLIENT)
    IMessage onReceive(MessageContext ctx) {
        TileEntity entity = Minecraft.getMinecraft().world.getTileEntity(pos);
        if (Minecraft.getMinecraft().world.provider.getDimension() == dim && entity instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) entity;
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                chest.setCount(new BigInteger(bytes));
                chest.setInventorySlotContents(1, out);
            });
        }
        return null;
    }
}
