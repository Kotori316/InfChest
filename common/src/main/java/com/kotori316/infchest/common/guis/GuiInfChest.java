package com.kotori316.infchest.common.guis;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;

public class GuiInfChest extends AbstractContainerScreen<ContainerInfChest> {
    private final TileInfChest infChest;
    private final static Identifier LOCATION = Identifier.fromNamespaceAndPath(InfChest.modID, "textures/gui/infchest.png");

    public GuiInfChest(ContainerInfChest container, Inventory inventory, Component component) {
        super(container, inventory, component);
        this.infChest = container.infChest;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Optional.ofNullable(infChest).map(TileInfChest::getHolding).filter(Predicate.not(ItemStack::isEmpty)).map(ItemStack::getDisplayName).ifPresent(itemName -> {
                graphics.text(this.font, itemName, (imageWidth - this.font.width(itemName)) / 2, 20, ARGB.opaque(0x404040), false);
                graphics.text(this.font, "Item: " + infChest.totalCount(), 8, 60, ARGB.opaque(0x404040), false);
            }
        );
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }
}
