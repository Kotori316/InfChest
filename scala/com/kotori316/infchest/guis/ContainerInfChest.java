package com.kotori316.infchest.guis;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class ContainerInfChest extends ScreenHandler {

    final TileInfChest infChest;

    public ContainerInfChest(int id, PlayerInventory playerInventory, BlockPos pos) {
        super(InfChest.Register.INF_CHEST_CONTAINER_TYPE, id);
        this.infChest = ((TileInfChest) playerInventory.player.getEntityWorld().getBlockEntity(pos));
        if (infChest != null)
            infChest.onOpen(playerInventory.player);
        int oneBox = 18;

        addSlot(new LimitSlot(infChest, 0, 31, 35));
        addSlot(new LimitSlot(infChest, 1, 127, 35));

        for (int h = 0; h < 3; ++h)
            for (int v = 0; v < 9; ++v)
                addSlot(new Slot(playerInventory, 9 + h * 9 + v, 8 + oneBox * v, 84 + oneBox * h));

        for (int v = 0; v < 9; ++v)
            addSlot(new Slot(playerInventory, v, 8 + oneBox * v, 142));
    }

    @Override
    public boolean canUse(PlayerEntity playerIn) {
        return infChest.canPlayerUse(playerIn);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerIn, int index) {
        Slot from = getSlot(index);
        if (from.hasStack()) {
            ItemStack current = from.getStack();
            int originalSize = current.getCount();
            int originalSlot = 2;
            if (index < originalSlot) {
                if (!this.insertItem(current, originalSlot, originalSlot + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (infChest.isValid(0, current)) {
                if (!this.insertItem(current, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (current.getCount() == 0)
                from.setStack(ItemStack.EMPTY);
            else
                from.markDirty();

            if (current.getCount() == originalSize)
                return ItemStack.EMPTY;

            from.onTakeItem(playerIn, current);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.infChest.onClose(player);
    }

    private static class LimitSlot extends Slot {

        LimitSlot(Inventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return inventory.isValid(getIndex(), stack);
        }

    }

    @SuppressWarnings("unused")
    public static ContainerInfChest create(int windowId, PlayerInventory inv, PacketByteBuf data) {
        return new ContainerInfChest(windowId, inv, data.readBlockPos());
    }
}
