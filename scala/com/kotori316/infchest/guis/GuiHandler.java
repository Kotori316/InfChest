package com.kotori316.infchest.guis;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.FMLPlayMessages;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class GuiHandler {
    public static final ResourceLocation CHEST_GUI_ID = new ResourceLocation(InfChest.modID, "gui_infchest");
//
//    @Nullable
//    @Override
//    public Container getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
//        BlockPos pos = new BlockPos(x, y, z);
//        TileEntity entity = world.getTileEntity(pos);
//        if (ID == CHEST_GUI_ID && entity instanceof TileInfChest) {
//            TileInfChest chest = (TileInfChest) entity;
//            return new ContainerInfChest(chest, player);
//        }
//        return null;
//    }

    @Nullable
    public static GuiContainer getClientGuiElement(FMLPlayMessages.OpenContainer message) {
        BlockPos pos = message.getAdditionalData().readBlockPos();
        TileEntity entity = Minecraft.getInstance().world.getTileEntity(pos);
        if (message.getId().equals(CHEST_GUI_ID) && entity instanceof TileInfChest) {
            TileInfChest chest = (TileInfChest) entity;
            return new GuiInfChest(chest, Minecraft.getInstance().player);
        }
        return null;
    }
}
