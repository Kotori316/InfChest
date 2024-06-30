package com.kotori316.infchest.forge.integration;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AE2InfChestIntegration {

    public static void onAPIAvailable() {
        if (ModList.get().isLoaded("ae2"))
            MinecraftForge.EVENT_BUS.register(new AE2InfChestIntegration());
    }

    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(InfChest.modID, "attach_ae2");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof TileInfChest infChest) {
            event.addCapability(LOCATION, new AE2Capability(infChest));
        }
    }
}

class AE2Capability implements ICapabilityProvider {

    private static final Capability<MEStorage> ME_STORAGE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final LazyOptional<MEStorage> accessorLazyOptional;

    AE2Capability(TileInfChest chest) {
        accessorLazyOptional = LazyOptional.of(() -> new AEInfChestInv(chest));
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ME_STORAGE_CAPABILITY.orEmpty(cap, accessorLazyOptional.cast());
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

