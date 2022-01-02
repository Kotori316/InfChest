package com.kotori316.infchest.integration;

import java.math.BigInteger;
import java.util.Objects;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEStorage;
import appeng.capabilities.Capabilities;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class AE2InfChestIntegration {

    public static void onAPIAvailable() {
        if (ModList.get().isLoaded("ae2"))
            MinecraftForge.EVENT_BUS.register(new AE2InfChestIntegration());
    }

    private static final ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "attach_ae2");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof TileInfChest infChest) {
            event.addCapability(LOCATION, new AE2Capability(infChest));
        }
    }
}

class AE2Capability implements ICapabilityProvider {

    private final LazyOptional<IStorageMonitorableAccessor> accessorLazyOptional;

    AE2Capability(TileInfChest chest) {
        accessorLazyOptional = LazyOptional.of(() -> new AEInfChestInv(chest));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return Capabilities.STORAGE_MONITORABLE_ACCESSOR.orEmpty(cap, accessorLazyOptional.cast());
    }

}

record AEInfChestInv(TileInfChest chest) implements MEStorage, IStorageMonitorableAccessor {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(9000000000000000000L);

    // IStorageMonitorableAccessor
    @Override
    public MEStorage getInventory(IActionSource iActionSource) {
        return this;
    }

    // MEStorage

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (what instanceof AEItemKey itemKey) {
            return this.chest.canPlaceItem(0, itemKey.toStack());
        } else {
            return false;
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey)) return 0; // Key is not item.
        var definition = itemKey.toStack();
        if (!this.chest.canPlaceItem(0, definition)) return 0; // The item is NOT acceptable.
        if (mode == Actionable.MODULATE) {
            chest.addStack(definition, BigInteger.valueOf(amount));
            chest.setChanged();
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey)) return 0; // Key is not item.
        var definition = itemKey.toStack();
        var holding = chest.getStack();
        var out = chest.getItem(1);
        if (ItemStack.isSameItemSameTags(definition, holding)) {
            BigInteger extractCount = BigInteger.valueOf(amount).min(chest.itemCount());
            if (mode == Actionable.MODULATE) {
                // do subtract.
                chest.decrStack(extractCount);
                chest.setChanged();
            }
            if (extractCount.equals(chest.itemCount())) {
                // The caller requests more items than this chest holds.
                // Check the output slot and extract from it.
                if (ItemStack.isSameItemSameTags(definition, out)) {
                    var extraCount = BigInteger.valueOf(amount).subtract(chest.itemCount()).min(BigInteger.valueOf(out.getCount())).intValueExact();
                    if (mode == Actionable.MODULATE) {
                        this.chest.removeItem(1, extraCount);
                        this.chest.setChanged();
                    }
                    return extractCount.longValue() + extraCount;
                } else {
                    // There is no extra item to be extracted.
                    return extractCount.longValue();
                }
            } else {
                // The demand is satisfied.
                return extractCount.longValue();
            }
        } else if (ItemStack.isSameItemSameTags(definition, out)) {
            int extractCount = (int) Math.min(out.getCount(), amount);
            if (mode == Actionable.MODULATE) {
                this.chest.removeItem(1, extractCount);
                this.chest.setChanged();
            }
            return extractCount;
        } else {
            return 0; // This chest doesn't contain the item.
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var inSlot = this.chest.getItem(1);
        if (!inSlot.isEmpty()) {
            out.add(Objects.requireNonNull(AEItemKey.of(inSlot)), inSlot.getCount());
        }
        var holding = this.chest.getStack();
        if (!holding.isEmpty()) {
            var count = LONG_MAX.min(this.chest.itemCount());
            out.add(Objects.requireNonNull(AEItemKey.of(holding)), count.longValue());
        }
    }

    @Override
    public Component getDescription() {
        return this.chest.getName();
    }
}

