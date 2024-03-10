package com.kotori316.infchest.common.guis;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

public class GuiInfChest extends AbstractContainerScreen<ContainerInfChest> {
    private final TileInfChest infChest;
    private final static ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "textures/gui/infchest.png");

    public GuiInfChest(ContainerInfChest container, Inventory inventory, Component component) {
        super(container, inventory, component);
        this.infChest = container.infChest;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        Optional.ofNullable(infChest).map(TileInfChest::getHolding).filter(Predicate.not(ItemStack::isEmpty)).map(ItemStack::getDisplayName).ifPresent(itemName -> {
                graphics.drawString(this.font, itemName, (imageWidth - this.font.width(itemName)) / 2, 20, 0x404040, false);
                graphics.drawString(this.font, "Item: " + infChest.totalCount(), 8, 60, 0x404040, false);
            }
        );
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);// background
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY); // render tooltip
    }
}
