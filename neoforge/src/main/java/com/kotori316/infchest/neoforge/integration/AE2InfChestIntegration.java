package com.kotori316.infchest.neoforge.integration;

import appeng.api.AECapabilities;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.integration.CommonAE2Part;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class AE2InfChestIntegration {

    public static void onAPIAvailable(IEventBus modBus) {
        if (ModList.get().isLoaded("ae2")) {
            modBus.register(new AE2Capability());
        }
    }
}

class AE2Capability {
    @SubscribeEvent
    public void attachCapability(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(AECapabilities.ME_STORAGE, InfChest.accessor.INF_CHEST_TYPE(), AE2Capability::create);
    }

    private static MEStorage create(TileInfChest chest, Direction ignored) {
        return new AEInfChestInv(chest);
    }
}

record AEInfChestInv(TileInfChest chest) implements MEStorage {

    // MEStorage

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        if (what instanceof AEItemKey itemKey) {
            return CommonAE2Part.isPreferredStorageFor(this.chest, itemKey.toStack());
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
        CommonAE2Part.getAvailableStacks(AEItemKey::of, out::add, this.chest);
    }

    @Override
    public Component getDescription() {
        return this.chest.getName();
    }
}

