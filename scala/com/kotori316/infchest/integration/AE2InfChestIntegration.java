package com.kotori316.infchest.integration;

import java.math.BigInteger;
import java.util.Objects;

import appeng.api.IAEAddonEntrypoint;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.MEStorage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class AE2InfChestIntegration implements IAEAddonEntrypoint {

    @Override
    public void onAe2Initialized() {
        if (FabricLoader.getInstance().isModLoaded("ae2")) {
            AE2Capability.event();
        }
    }
}

class AE2Capability {
    public static void event() {
        IStorageMonitorableAccessor.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof TileInfChest chest) return new AEInfChestInv(chest);
            else return null;
        }, InfChest.Register.INF_CHEST_TYPE);
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
            return this.chest.isValid(0, itemKey.toStack());
        } else {
            return false;
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey)) return 0; // Key is not item.
        var definition = itemKey.toStack();
        if (!this.chest.isValid(0, definition)) return 0; // The item is NOT acceptable.
        if (mode == Actionable.MODULATE) {
            chest.addStack(definition, BigInteger.valueOf(amount));
            chest.markDirty();
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey)) return 0; // Key is not item.
        var definition = itemKey.toStack();
        var holding = chest.getStackWithAmount();
        var out = chest.getStack(1);
        if (ItemStack.canCombine(definition, holding)) {
            BigInteger extractCount = BigInteger.valueOf(amount).min(chest.itemCount());
            if (mode == Actionable.MODULATE) {
                // do subtract.
                chest.decrStack(extractCount);
                chest.markDirty();
            }
            if (extractCount.equals(chest.itemCount())) {
                // The caller requests more items than this chest holds.
                // Check the output slot and extract from it.
                if (ItemStack.canCombine(definition, out)) {
                    var extraCount = BigInteger.valueOf(amount).subtract(chest.itemCount()).min(BigInteger.valueOf(out.getCount())).intValueExact();
                    if (mode == Actionable.MODULATE) {
                        this.chest.removeStack(1, extraCount);
                        this.chest.markDirty();
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
        } else if (ItemStack.canCombine(definition, out)) {
            int extractCount = (int) Math.min(out.getCount(), amount);
            if (mode == Actionable.MODULATE) {
                this.chest.removeStack(1, extractCount);
                this.chest.markDirty();
            }
            return extractCount;
        } else {
            return 0; // This chest doesn't contain the item.
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        var inSlot = this.chest.getStack(1);
        if (!inSlot.isEmpty()) {
            out.add(Objects.requireNonNull(AEItemKey.of(inSlot)), inSlot.getCount());
        }
        var holding = this.chest.getStackWithAmount();
        if (!holding.isEmpty()) {
            var count = LONG_MAX.min(this.chest.itemCount());
            out.add(Objects.requireNonNull(AEItemKey.of(holding)), count.longValue());
        }
    }

    @Override
    public Text getDescription() {
        return this.chest.getName();
    }
}
