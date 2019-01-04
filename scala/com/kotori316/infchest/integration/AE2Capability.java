package com.kotori316.infchest.integration;

import java.math.BigInteger;
import java.util.Optional;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.capabilities.Capabilities;
import appeng.me.helpers.BaseActionSource;
import appeng.me.storage.MEMonitorPassThrough;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.kotori316.infchest.tiles.TileInfChest;

public class AE2Capability implements ICapabilityProvider {
    private static final ResourceLocation LOCATION = new ResourceLocation(AE2.modID, "attach_ae2");
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TileInfChest) {
            event.addCapability(LOCATION, new AE2Capability((TileInfChest) event.getObject()));
        }
    }

    private final TileInfChest chest;
    private AEInfChestInv inv;

    public AE2Capability(TileInfChest chest) {
        this.chest = chest;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR) {
            if (inv == null) {
                inv = new AEInfChestInv();
                this.chest.addUpdate(() -> inv.postChange());
            }
            return Capabilities.STORAGE_MONITORABLE_ACCESSOR.cast(inv);
        }
        return null;
    }

    private class AEInfChestInv implements IMEInventory<IAEItemStack>, IStorageMonitorableAccessor {
        private IStorageMonitorable monitorable = new IStorageMonitorable() {
            @SuppressWarnings("unchecked")
            @Override
            public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> iStorageChannel) {
                if (iStorageChannel == getChannel()) {
                    mePassThrough.setInternal(AEInfChestInv.this);
                    return (IMEMonitor<T>) mePassThrough;
                } else {
                    return null;
                }
            }
        };
        private MEMonitorPassThrough<IAEItemStack> mePassThrough = new MEMonitorPassThrough<>(null, getChannel());

        @Override
        public IAEItemStack injectItems(IAEItemStack iaeItemStack, Actionable actionable, IActionSource iActionSource) {
            ItemStack definition = iaeItemStack.getDefinition();
            if (chest.isItemValidForSlot(0, definition)) {
                if (actionable == Actionable.MODULATE) {
                    // do fill
                    chest.addStack(definition, BigInteger.valueOf(iaeItemStack.getStackSize()));
                    chest.markDirty();
                }
                return getChannel().createStack(ItemStack.EMPTY);
            } else {
                // Not acceptable.
                return iaeItemStack;
            }
        }

        @Override
        public IAEItemStack extractItems(IAEItemStack iaeItemStack, Actionable actionable, IActionSource iActionSource) {
            ItemStack definition = iaeItemStack.getDefinition();
            ItemStack holding = chest.getStack();
            if (ItemStack.areItemsEqual(holding, definition) && ItemStack.areItemStackTagsEqual(holding, definition)) {
                BigInteger subs = chest.itemCount().min(BigInteger.valueOf(iaeItemStack.getStackSize()));
                if (actionable == Actionable.MODULATE) {
                    // do substract.
                    chest.decrStack(subs);
                    chest.markDirty();
                }
                IAEItemStack stack = iaeItemStack.copy();
                stack.setStackSize(subs.min(LONG_MAX).longValueExact());
                return stack;
            }
            return getChannel().createStack(ItemStack.EMPTY);
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> iItemList) {
            Optional.ofNullable(getChannel().createStack(chest.getStack()))
                .map(s -> s.setStackSize(chest.itemCount().min(LONG_MAX).longValueExact()))
                .ifPresent(iItemList::add);
            return iItemList;
        }

        @Override
        public IStorageChannel<IAEItemStack> getChannel() {
            return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
        }

        @Override
        public IStorageMonitorable getInventory(IActionSource iActionSource) {
            return monitorable;
        }

        public void postChange() {
            mePassThrough.postChange(mePassThrough, mePassThrough.getStorageList(), new BaseActionSource());
        }
    }
}
