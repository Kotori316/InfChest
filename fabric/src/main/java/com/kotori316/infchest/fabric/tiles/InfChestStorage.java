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

@SuppressWarnings("UnstableApiUsage")
public final class InfChestStorage extends SnapshotParticipant<InfChestStorage.ChestItems> implements SingleSlotStorage<ItemVariant> {
    private final TileInfChestFabric chest;

    @VisibleForTesting
    InfChestStorage(TileInfChestFabric chest) {
        this.chest = chest;
    }

    @Override
    protected ChestItems createSnapshot() {
        return new ChestItems(chest.getItem(0), chest.getHoldingWithOneCount().copy(), chest.totalCount());
    }

    @Override
    protected void readSnapshot(ChestItems snapshot) {
        chest.decrStack(chest.totalCount());
        chest.addStack(snapshot.holding, snapshot.count);
        chest.setItem(0, snapshot.inputSlot);
        chest.setChanged();
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
        long extracted;
        if (resource.matches(chest.getHoldingWithOneCount())) {
            var toTake = BigInteger.valueOf(maxAmount);
            if (chest.totalCount().compareTo(toTake) >= 0) {
                // chest.itemCount() >= maxAmount
                extracted = maxAmount;
                chest.decrStack(toTake);
            } else {
                extracted = chest.totalCount().longValueExact();
                chest.decrStack(chest.totalCount());
            }
        } else {
            extracted = 0;
        }
        return extracted;
    }

    @Override
    public boolean isResourceBlank() {
        return chest.totalCount().equals(BigInteger.ZERO);
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(chest.getHoldingWithOneCount());
    }

    @Override
    public long getAmount() {
        return chest.totalCount().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    protected record ChestItems(ItemStack inputSlot, ItemStack holding, BigInteger count) {
    }

    public static void register() {
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity instanceof TileInfChestFabric chest) return new InfChestStorage(chest);
            else return null;
        }, InfChest.accessor.CHEST());
    }
}
