package com.kotori316.infchest.fabric.integration;

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
import net.minecraft.network.chat.Component;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.integration.CommonAE2Part;
import com.kotori316.infchest.common.tiles.TileInfChest;

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
        }, InfChest.accessor.INF_CHEST_TYPE());
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
        return CommonAE2Part.insert(this.chest, amount, definition, mode == Actionable.MODULATE);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (!(what instanceof AEItemKey itemKey)) return 0; // Key is not item.
        var definition = itemKey.toStack();
        return CommonAE2Part.extract(this.chest, amount, definition, mode == Actionable.MODULATE);
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
