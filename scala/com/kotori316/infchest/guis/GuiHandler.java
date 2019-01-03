package com.kotori316.infchest.guis;

import javax.annotation.Nullable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.kotori316.infchest.tiles.TileInfChest;

public class GuiHandler implements IGuiHandler {
    public static final int CHEST_GUI_ID = 0;

    @Nullable
    @Override
    public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity entity = world.getTileEntity(pos);
        if (ID == CHEST_GUI_ID && entity instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) entity;
            return new ContainerInfChest(chest, player);
        }
        return null;
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity entity = world.getTileEntity(pos);
        if (ID == CHEST_GUI_ID && entity instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) entity;
            return new GuiInfChest(chest, player);
        }
        return null;
    }
}
