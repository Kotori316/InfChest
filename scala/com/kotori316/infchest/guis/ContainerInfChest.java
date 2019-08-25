package com.kotori316.infchest.guis;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class ContainerInfChest extends Container {

    final TileInfChest infChest;

    public ContainerInfChest(int id, PlayerInventory playerInventory, BlockPos pos) {
        super(InfChest.INF_CHEST_CONTAINER_TYPE, id);
        this.infChest = ((TileInfChest) playerInventory.player.getEntityWorld().getTileEntity(pos));
        infChest.openInventory(playerInventory.player);
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
    public boolean canInteractWith(PlayerEntity playerIn) {
        return infChest.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
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

    @SuppressWarnings("unused")
    public static ContainerInfChest create(int windowId, PlayerInventory inv, PacketBuffer data) {
        return new ContainerInfChest(windowId, inv, data.readBlockPos());
    }
}
