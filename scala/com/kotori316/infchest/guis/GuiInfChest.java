package com.kotori316.infchest.guis;

import java.util.Optional;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
        String s = infChest.hasCustomName() ? infChest.getName() : InfChest.CHEST.getUnlocalizedName() + ".name";
        String format = I18n.format(s);
        this.fontRenderer.drawString(format, this.xSize / 2 - this.fontRenderer.getStringWidth(format) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        Optional.of(infChest.getStack()).filter(InfChest.STACK_NON_EMPTY).map(ItemStack::getDisplayName).ifPresent(itemName -> {
                this.fontRenderer.drawString(itemName, this.xSize / 2 - this.fontRenderer.getStringWidth(itemName) / 2, 35, 0x404040);
                this.fontRenderer.drawString("Item: " + infChest.itemCount(), 8, 60, 0x404040);
            }
        );
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
