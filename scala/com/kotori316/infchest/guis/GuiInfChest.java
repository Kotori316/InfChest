package com.kotori316.infchest.guis;

import java.math.BigInteger;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class GuiInfChest extends AbstractContainerScreen<ContainerInfChest> {
    private final TileInfChest infChest;
    private final static ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "textures/gui/infchest.png");

    public GuiInfChest(ContainerInfChest container, Inventory inventory, Component component) {
        super(container, inventory, component);
        this.infChest = container.infChest;
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, final int mouseX, final int mouseY) {
        super.renderLabels(matrixStack, mouseX, mouseY);
        Optional.ofNullable(infChest).map(TileInfChest::getStackWithAmount).filter(Predicate.not(ItemStack::isEmpty)).map(ItemStack::getHoverName).ifPresent(itemName -> {
                this.font.draw(matrixStack, itemName.getString(), (this.imageWidth - this.font.width(itemName)) / (float) 2, 20, 0x404040);
                var count = infChest.itemCount().add(BigInteger.valueOf(infChest.getItem(1).getCount()));
                this.font.draw(matrixStack, "Item: " + count, titleLabelX, 60, 0x404040);
            }
        );
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.blit(matrixStack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);

    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }
}
