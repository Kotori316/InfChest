package com.kotori316.infchest.integration;

import com.kotori316.infchest.tiles.TileInfChest;
import com.refinedmods.refinedstorage.api.IRSAPI;
import com.refinedmods.refinedstorage.api.RSAPIInject;
import com.refinedmods.refinedstorage.api.network.INetwork;
import com.refinedmods.refinedstorage.api.storage.AccessType;
import com.refinedmods.refinedstorage.api.storage.StorageType;
import com.refinedmods.refinedstorage.api.storage.externalstorage.IExternalStorage;
import com.refinedmods.refinedstorage.api.storage.externalstorage.IExternalStorageContext;
import com.refinedmods.refinedstorage.api.storage.externalstorage.IExternalStorageProvider;
import com.refinedmods.refinedstorage.api.util.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public final class RsInfChestIntegration {
    public static void onAPIAvailable() {
        if (ModList.get().isLoaded("refinedstorage")) {
            RsAccess.registerProvider();
        }
    }

    public static final class RsAccess {
        @RSAPIInject
        public static IRSAPI RS_API;

        static void registerProvider() {
            RS_API.addExternalStorageProvider(StorageType.ITEM, new RsInfChestProvider());
        }
    }

}

final class RsInfChestProvider implements IExternalStorageProvider<ItemStack> {
    @Override
    public boolean canProvide(BlockEntity blockEntity, Direction direction) {
        return blockEntity instanceof TileInfChest;
    }

    @NotNull
    @Override
    public IExternalStorage<ItemStack> provide(IExternalStorageContext context, BlockEntity entity, Direction direction) {
        return new RsInfChestInv((TileInfChest) entity, context, new AtomicReference<>(null));
    }

    @Override
    public int getPriority() {
        return 10;
    }
}

record RsInfChestInv(TileInfChest chest, IExternalStorageContext context,
                     AtomicReference<ItemStack> cache) implements IExternalStorage<ItemStack> {

    @Override
    public void update(INetwork network) {
        if (this.getAccessType() != AccessType.INSERT) {
            ItemStack cached = cache.get();
            if (cached == null) {
                // first cycle
                ItemStack holding = chest.getHolding();
                cache.set(holding);
                return;
            }

            ItemStack holding = chest.getHolding();
            if (!ItemStack.matches(cached, holding)) {
                if (!cached.isEmpty()) {
                    network.getItemStorageCache().remove(cached, cached.getCount(), true);
                }
                if (!holding.isEmpty()) {
                    network.getItemStorageCache().add(ItemHandlerHelper.copyStackWithSize(holding, 1), holding.getCount(), false, true);
                    cache.set(holding);
                } else {
                    cache.set(ItemStack.EMPTY);
                }
                network.getItemStorageCache().flush();
            }
        }
    }

    @Override
    public long getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Collection<ItemStack> getStacks() {
        return Collections.singleton(chest.getHolding());
    }

    @NotNull
    @Override
    public ItemStack insert(@NotNull ItemStack stack, int size, Action action) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (!chest.canPlaceItem(0, stack)) return ItemHandlerHelper.copyStackWithSize(stack, size);

        if (action == Action.PERFORM) {
            chest.addStack(stack, BigInteger.valueOf(size));
            chest.setChanged();
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack extract(@NotNull ItemStack stack, int size, int flags, Action action) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack holding = chest.getHolding();
        if (!RsInfChestIntegration.RsAccess.RS_API.getComparer().isEqual(stack, holding, flags)) return ItemStack.EMPTY;
        BigInteger extractCount = chest.totalCount().min(BigInteger.valueOf(size));

        if (action == Action.PERFORM) {
            chest.decrStack(extractCount);
            chest.setChanged();
        }
        return ItemHandlerHelper.copyStackWithSize(stack, extractCount.intValueExact());
    }

    @Override
    public int getStored() {
        return TileInfChest.INT_MAX.min(chest.totalCount()).intValueExact();
    }

    @Override
    public int getPriority() {
        return context.getPriority();
    }

    @Override
    public AccessType getAccessType() {
        return context.getAccessType();
    }

    @Override
    public int getCacheDelta(int storedPreInsertion, int size, @Nullable ItemStack remainder) {
        if (this.getAccessType() == AccessType.INSERT) {
            // No need to count if this container is insert only(no extraction available)
            return 0;
        } else {
            return remainder == null ? size : size - remainder.getCount();
        }
    }
}
