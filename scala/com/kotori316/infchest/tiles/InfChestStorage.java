package com.kotori316.infchest.tiles;

import java.math.BigInteger;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import com.kotori316.infchest.InfChest;

@SuppressWarnings("UnstableApiUsage")
public final class InfChestStorage extends SnapshotParticipant<InfChestStorage.ChestItems> implements SingleSlotStorage<ItemVariant> {
    private final TileInfChest chest;

    private InfChestStorage(TileInfChest chest) {
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
        if (resource.matches(chest.getHolding())) {
            updateSnapshots(transaction);
            chest.addStack(resource.toStack(1), BigInteger.valueOf(maxAmount));
            return maxAmount;
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long takeFromOutputSlot;
        if (resource.matches(chest.getHolding())) {
            var toTake = BigInteger.valueOf(maxAmount);
            if (chest.itemCount().compareTo(toTake) >= 0) {
                // chest.itemCount() >= maxAmount
                // takeFromOutputSlot = maxAmount;
                chest.decrStack(toTake);
                return maxAmount;
            } else {
                chest.decrStack(chest.itemCount());
                takeFromOutputSlot = chest.itemCount().longValueExact();
            }
        } else {
            takeFromOutputSlot = maxAmount;
        }
        long fromHolding = maxAmount - takeFromOutputSlot;
        if (ItemStack.isSameItemSameTags(chest.getItem(1), resource.toStack(1))) {
            // take from output slot.
            var extracted = chest.removeItem(1, (int) Math.min(takeFromOutputSlot, Integer.MAX_VALUE));
            return fromHolding + extracted.getCount();
        }
        return fromHolding;
    }

    @Override
    public boolean isResourceBlank() {
        return chest.itemCount().equals(BigInteger.ZERO);
    }

    @Override
    public ItemVariant getResource() {
        return ItemVariant.of(chest.getStackWithAmount(1));
    }

    @Override
    public long getAmount() {
        return chest.itemCount().min(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    record ChestItems(ItemStack inputSlot, ItemStack outputSlot, ItemStack holding, BigInteger count) {
    }

    public static void register() {
        ItemStorage.SIDED.registerForBlocks((world, pos, state, blockEntity, context) -> {
            if (blockEntity instanceof TileInfChest chest) return new InfChestStorage(chest);
            else return null;
        }, InfChest.Register.CHEST);
    }
}
