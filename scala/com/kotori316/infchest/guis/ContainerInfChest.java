package com.kotori316.infchest.guis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.kotori316.infchest.tiles.TileInfChest;

public class ContainerInfChest extends Container {

    private final TileInfChest infChest;

    public ContainerInfChest(TileInfChest infChest, EntityPlayer player) {
        this.infChest = infChest;
        int oneBox = 18;

        addSlot(new LimitSlot(infChest, 0, 31, 35));
        addSlot(new LimitSlot(infChest, 1, 127, 35));

        for (int h = 0; h < 3; ++h)
            for (int v = 0; v < 9; ++v)
                addSlot(new Slot(player.inventory, 9 + h * 9 + v, 8 + oneBox * v, 84 + oneBox * h));

        for (int v = 0; v < 9; ++v)
            addSlot(new Slot(player.inventory, v, 8 + oneBox * v, 142));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return infChest.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        Slot from = getSlot(index);
        if (from.getHasStack()) {
            ItemStack current = from.getStack();
            int originalSize = current.getCount();
            int originalSlot = 2;
            if (index < originalSlot) {
                if (!this.mergeItemStack(current, originalSlot, originalSlot + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (infChest.isItemValidForSlot(0, current)) {
                if (!this.mergeItemStack(current, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (current.getCount() == 0)
                from.putStack(ItemStack.EMPTY);
            else
                from.onSlotChanged();

            if (current.getCount() == originalSize)
                return ItemStack.EMPTY;

            from.onTake(playerIn, current);
        }
        return ItemStack.EMPTY;
    }

    private static class LimitSlot extends Slot {

        LimitSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }

    }
}
