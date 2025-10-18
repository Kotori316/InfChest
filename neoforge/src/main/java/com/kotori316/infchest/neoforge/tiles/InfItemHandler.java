package com.kotori316.infchest.neoforge.tiles;

import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

final class InfItemHandler extends SnapshotJournal<TileInfChest.ChestItems> implements ResourceHandler<ItemResource> {
    private final TileInfChestNeoForge infChest;

    InfItemHandler(TileInfChestNeoForge infChest) {
        this.infChest = infChest;
    }

    @Override
    protected TileInfChest.ChestItems createSnapshot() {
        return infChest.getSnapshot();
    }

    @Override
    protected void revertToSnapshot(TileInfChest.ChestItems snapshot) {
        infChest.readSnapshot(snapshot);
    }

    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        infChest.setItem(slot, stack);
    }

    @Override
    public int size() {
        return infChest.getContainerSize();
    }

    @NotNull
    @Override
    public ItemResource getResource(int slot) {
        return ItemResource.of(infChest.getItem(slot));
    }

    @Override
    public long getAmountAsLong(int index) {
        return infChest.getItem(index).getCount();
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (isValid(0, resource)) {
            updateSnapshots(transaction);
            infChest.addStack(resource.toStack(amount));
            infChest.setChanged();
            return amount;
        }
        return 0;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        if (slot != 1) {
            // Only slot 1 allows extracting
            return 0;
        }
        var item = infChest.getHolding();
        if (item.isEmpty()) {
            // Nothing to extract
            return 0;
        }

        updateSnapshots(transaction);
        var extractCount = infChest.totalCount().min(BigInteger.valueOf(amount));
        infChest.decrStack(extractCount);
        infChest.setChanged();
        return extractCount.intValueExact();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return infChest.getMaxStackSize();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return infChest.canPlaceItem(index, resource.toStack());
    }

    public TileInfChestNeoForge infChest() {
        return infChest;
    }
}
