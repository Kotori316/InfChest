package com.kotori316.infchest.guis;

import java.math.BigInteger;
import java.util.Optional;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class GuiInfChest extends HandledScreen<ContainerInfChest> {
    private final TileInfChest infChest;
    private final static Identifier LOCATION = new Identifier(InfChest.modID, "textures/gui/infchest.png");

    public GuiInfChest(ContainerInfChest container, PlayerInventory inventory, Text component) {
        super(container, inventory, component);
        this.infChest = container.infChest;
    }

    @Override
    protected void drawForeground(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.drawForeground(matrixStack, mouseX, mouseY);
        Optional.ofNullable(infChest).map(TileInfChest::getStackWithAmount).filter(InfChest.STACK_NON_EMPTY).map(ItemStack::getName).ifPresent(itemName -> {
                this.textRenderer.draw(matrixStack, itemName.getString(), (this.backgroundWidth - this.textRenderer.getWidth(itemName)) / (float) 2, 20, 0x404040);
                var count = infChest.itemCount().add(BigInteger.valueOf(infChest.getStack(1).getCount()));
                this.textRenderer.draw(matrixStack, "Item: " + count, titleX, 60, 0x404040);
            }
        );
    }

    @Override
    protected void drawBackground(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.drawTexture(matrixStack, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}
