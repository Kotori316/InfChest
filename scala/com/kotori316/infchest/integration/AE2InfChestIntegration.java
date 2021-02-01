package com.kotori316.infchest.integration;

import java.math.BigInteger;
import java.util.Optional;

import appeng.api.AEAddon;
import appeng.api.IAEAddon;
import appeng.api.IAppEngApi;
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
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

import static com.kotori316.infchest.tiles.TileInfChest.INT_MAX;

@AEAddon
public class AE2InfChestIntegration implements IAEAddon {
    private IAppEngApi api;

    @Override
    public void onAPIAvailable(IAppEngApi iAppEngApi) {
        this.api = iAppEngApi;
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static final ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "attach_ae2");

    @SubscribeEvent
    public void attachCapability(AttachCapabilitiesEvent<TileEntity> event) {
        if (event.getObject() instanceof TileInfChest) {
            event.addCapability(LOCATION, new AE2Capability((TileInfChest) event.getObject(), api));
        }
    }
}

class AE2Capability implements ICapabilityProvider {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(9000000000000000000L);

    private final TileInfChest chest;
    private final IAppEngApi api;
    private final LazyOptional<IStorageMonitorableAccessor> accessorLazyOptional = LazyOptional.of(AEInfChestInv::new);

    AE2Capability(TileInfChest chest, IAppEngApi api) {
        this.chest = chest;
        this.api = api;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return Capabilities.STORAGE_MONITORABLE_ACCESSOR.orEmpty(cap, accessorLazyOptional.cast());
    }

    class AEInfChestInv implements IMEInventory<IAEItemStack>, IStorageMonitorableAccessor {
        private final IStorageMonitorable monitorable = new IStorageMonitorable() {
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
        private final MEMonitorPassThrough<IAEItemStack> mePassThrough = new MEMonitorPassThrough<>(null, getChannel());

        AEInfChestInv() {
            AE2Capability.this.chest.addUpdate(this::postChange);
        }

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
            ItemStack out = chest.getStackInSlot(1);
            BigInteger requestSize = BigInteger.valueOf(iaeItemStack.getStackSize());
            if (ItemStack.areItemsEqual(holding, definition) && ItemStack.areItemStackTagsEqual(holding, definition)) {
                BigInteger subs = chest.itemCount().min(requestSize);
                if (actionable == Actionable.MODULATE) {
                    // do subtract.
                    chest.decrStack(subs);
                    chest.markDirty();
                }
                if (subs.compareTo(requestSize) < 0) {
                    if (ItemStack.areItemsEqual(out, definition) && ItemStack.areItemStackTagsEqual(out, definition)) {
                        ItemStack decreased;
                        if (actionable == Actionable.MODULATE) {
                            decreased = chest.decrStackSize(1, (requestSize.subtract(subs)).min(BigInteger.valueOf(definition.getMaxStackSize())).intValueExact());
                        } else {
                            decreased = out.copy();
                            decreased.setCount((requestSize.subtract(subs)).min(BigInteger.valueOf(decreased.getCount())).intValueExact());
                        }
                        subs = subs.add(BigInteger.valueOf(decreased.getCount()));
                    }
                }
                IAEItemStack stack = iaeItemStack.copy();
                stack.setStackSize(subs.min(LONG_MAX).longValueExact());
                return stack;
            } else if (ItemStack.areItemsEqual(out, definition) && ItemStack.areItemStackTagsEqual(out, definition)) {
                BigInteger subs = BigInteger.valueOf(out.getCount()).min(requestSize);
                ItemStack decreased;
                if (actionable == Actionable.MODULATE) {
                    decreased = chest.decrStackSize(1, subs.min(INT_MAX).intValueExact());
                } else {
                    decreased = out.copy();
                    decreased.setCount(subs.min(BigInteger.valueOf(decreased.getCount())).intValueExact());
                }
                return getChannel().createStack(decreased);
            }
            return getChannel().createStack(ItemStack.EMPTY);
        }

        @Override
        public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> iItemList) {
            ItemStack inSlot = chest.getStackInSlot(1);
            Optional.of(chest.getStack()).filter(InfChest.STACK_NON_EMPTY).map(getChannel()::createStack)
                .map(s -> s.setStackSize(chest.itemCount().min(LONG_MAX.subtract(BigInteger.valueOf(inSlot.getCount()))).longValueExact()))
                .ifPresent(iItemList::add);
            Optional.of(inSlot).filter(InfChest.STACK_NON_EMPTY)
                .map(getChannel()::createStack)
                .ifPresent(iItemList::add);
            return iItemList;
        }

        @Override
        public IStorageChannel<IAEItemStack> getChannel() {
            return AE2Capability.this.api.storage().getStorageChannel(IItemStorageChannel.class);
        }

        @Override
        public IStorageMonitorable getInventory(IActionSource iActionSource) {
            return monitorable;
        }

        private void postChange() {
            mePassThrough.postChange(mePassThrough, mePassThrough.getStorageList(), new BaseActionSource());
        }
    }
}

