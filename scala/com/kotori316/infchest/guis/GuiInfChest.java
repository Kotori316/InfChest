package com.kotori316.infchest.guis;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GuiInfChest extends AbstractContainerScreen<ContainerInfChest> {
    private final TileInfChest infChest;
    private final static ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "textures/gui/infchest.png");

    public GuiInfChest(ContainerInfChest container, Inventory inventory, Component component) {
        super(container, inventory, component);
        this.infChest = container.infChest;
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        Optional.ofNullable(infChest).map(TileInfChest::getHolding).filter(InfChest.STACK_NON_EMPTY).map(ItemStack::getDisplayName).ifPresent(itemName -> {
                this.font.draw(matrixStack, itemName, (imageWidth - this.font.width(itemName)) / (float) 2, 20, 0x404040);
            this.font.draw(matrixStack, "Item: " + infChest.totalCount(), 8, 60, 0x404040);
            }
        );
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.blit(matrixStack, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack);// background
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY); // render tooltip
    }
}
