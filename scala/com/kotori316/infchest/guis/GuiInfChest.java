package com.kotori316.infchest.guis;

import java.util.Optional;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class GuiInfChest extends ContainerScreen<ContainerInfChest> {
    private final TileInfChest infChest;
    private final static ResourceLocation resourceLocation = new ResourceLocation(InfChest.modID, "textures/gui/infchest.png");

    public GuiInfChest(ContainerInfChest container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);
        this.infChest = container.infChest instanceof TileInfChest ? ((TileInfChest) container.infChest) : null;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = title.getFormattedText();
        String format = I18n.format(s);
        this.font.drawString(format, this.xSize / 2 - this.font.getStringWidth(format) / 2, 6, 4210752);
        this.font.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
        Optional.ofNullable(infChest).map(TileInfChest::getStack).filter(InfChest.STACK_NON_EMPTY).map(ItemStack::getDisplayName).ifPresent(itemName -> {
                String s1 = itemName.getFormattedText();
                this.font.drawString(s1, this.xSize / 2 - this.font.getStringWidth(s1) / 2, 35, 0x404040);
                this.font.drawString("Item: " + infChest.itemCount(), 8, 60, 0x404040);
            }
        );
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(resourceLocation);
        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;
        this.blit(k, l, 0, 0, xSize, ySize);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }
}
