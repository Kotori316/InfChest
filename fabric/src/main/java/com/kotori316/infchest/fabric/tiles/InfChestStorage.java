package com.kotori316.infchest.fabric.tiles;

import com.kotori316.infchest.common.InfChest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.VisibleForTesting;

import java.math.BigInteger;

public final class InfChestStorage extends SnapshotParticipant<InfChestStorage.ChestItems> implements SingleSlotStorage<ItemVariant> {
    private final TileInfChestFabric chest;

    @VisibleForTesting
    InfChestStorage(TileInfChestFabric chest) {
        this.chest = chest;
    }

    @Override
    protected ChestItems createSnapshot() {
        return new ChestItems(chest.getItem(0), chest.getItem(1), chest.getHolding().copy(), chest.itemCount());
    }

    @Override
    protected void readSnapshot(ChestItems snapshot) {
        chest.decrStack(chest.itemCount());
        chest.addStack(snapshot.holding, snapshot.count);
        chest.setItem(0, snapshot.inputSlot);
        chest.setItem(1, snapshot.outputSlot);
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.matches(chest.getHolding()) || chest.getHolding().isEmpty()) {
            updateSnapshots(transaction);
            chest.addStack(resource.toStack(1), BigInteger.valueOf(maxAmount));
            return maxAmount;
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        updateSnapshots(transaction);
        long takeFromOutputSlot;
        if (resource.matches(chest.getHolding())) {
            var toTake = BigInteger.valueOf(maxAmount);
            if (chest.itemCount().compareTo(toTake) >= 0) {
                // chest.itemCount() >= maxAmount
                // takeFromOutputSlot = maxAmount;
                chest.decrStack(toTake);
                return maxAmount;
            } else {
                takeFromOutputSlot = chest.itemCount().longValueExact();
                chest.decrStack(chest.itemCount());
            }
        } else {
            takeFromOutputSlot = 0;
        }
        long fromHolding = maxAmount - takeFromOutputSlot;
        if (ItemStack.isSameItemSameTags(chest.getItem(1), resource.toStack(1))) {
            // take from output slot.
            var extracted = chest.removeItem(1, (int) Math.min(fromHolding, Integer.MAX_VALUE));
            return takeFromOutputSlot + extracted.getCount();
        }
        return takeFromOutputSlot;
    }

    @Override
    public boolean isResourceBlank() {
        return chest.itemCount().equals(BigInteger.ZERO);
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(chest.getStack(1));
    }

    @Override
    public long getAmount() {
        return chest.itemCount().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    protected record ChestItems(ItemStack inputSlot, ItemStack outputSlot, ItemStack holding, BigInteger count) {
    }

    public static void register() {
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity instanceof TileInfChestFabric chest) return new InfChestStorage(chest);
            else return null;
        }, InfChest.accessor.CHEST());
    }
}
