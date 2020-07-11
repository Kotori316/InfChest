package com.kotori316.infchest.guis;

import java.util.Optional;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class GuiInfChest extends ContainerScreen<ContainerInfChest> {
    private final TileInfChest infChest;
    private final static ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "textures/gui/infchest.png");

    public GuiInfChest(ContainerInfChest container, PlayerInventory inventory, ITextComponent component) {
        super(container, inventory, component);
        this.infChest = container.infChest;
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.func_230451_b_(matrixStack, mouseX, mouseY);
        Optional.ofNullable(infChest).map(TileInfChest::getStack).filter(InfChest.STACK_NON_EMPTY).map(ItemStack::getDisplayName).ifPresent(itemName -> {
                this.field_230712_o_.func_238422_b_(matrixStack, itemName, (xSize - this.field_230712_o_.func_238414_a_(itemName)) / (float) 2, 35, 0x404040);
                this.field_230712_o_.func_238421_b_(matrixStack, "Item: " + infChest.itemCount(), 8, 60, 0x404040);
            }
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.func_230446_a_(matrixStack);// back ground
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }
}
