package com.kotori316.infchest.guis;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class ContainerInfChest extends AbstractContainerMenu {

    final TileInfChest infChest;

    public ContainerInfChest(int id, Inventory playerInventory, BlockPos pos) {
        super(InfChest.Register.INF_CHEST_CONTAINER_TYPE, id);
        this.infChest = ((TileInfChest) playerInventory.player.level.getBlockEntity(pos));
        if (infChest != null)
            infChest.startOpen(playerInventory.player);
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
    public boolean stillValid(Player playerIn) {
        return infChest.stillValid(playerIn);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot from = getSlot(index);
        if (from.hasItem()) {
            ItemStack current = from.getItem();
            int originalSize = current.getCount();
            int originalSlot = 2;
            if (index < originalSlot) {
                if (!this.moveItemStackTo(current, originalSlot, originalSlot + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (infChest.canPlaceItem(0, current)) {
                if (!this.moveItemStackTo(current, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (current.getCount() == 0)
                from.set(ItemStack.EMPTY);
            else
                from.setChanged();

            if (current.getCount() == originalSize)
                return ItemStack.EMPTY;

            from.onTake(playerIn, current);
        }
        return ItemStack.EMPTY;
    }

    private static class LimitSlot extends Slot {

        LimitSlot(Container inventoryIn, int index, int xPosition, int yPosition) {
            super(inventoryIn, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getSlotIndex(), stack);
        }

    }

    @SuppressWarnings("unused")
    public static ContainerInfChest create(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ContainerInfChest(windowId, inv, data.readBlockPos());
    }
}
