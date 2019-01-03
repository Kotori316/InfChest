package com.kotori316.infchest.guis;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class GuiInfChest extends GuiContainer {
    private final TileInfChest infChest;
    private final static ResourceLocation resourceLocation = new ResourceLocation(InfChest.modID, "textures/gui/infchest.png");

    GuiInfChest(TileInfChest infChest, EntityPlayer player) {
        super(new ContainerInfChest(infChest, player));
        this.infChest = infChest;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = InfChest.CHEST.getLocalizedName();
        this.fontRenderer.drawString(I18n.format(s), this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        this.fontRenderer.drawString("Item: " + infChest.itemCount(), 64, 35, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(resourceLocation);
        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
