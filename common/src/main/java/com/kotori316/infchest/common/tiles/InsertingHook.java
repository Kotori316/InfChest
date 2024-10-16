package com.kotori316.infchest.common.tiles;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record InsertingHook(List<Hook> hooks) {
    private static final List<Hook> DEFAULT_HOOKS;

    static {
        List<Hook> list = new ArrayList<>();
        // Storage Box
        /*if (InfChest.accessor.isModLoaded(StorageBoxStack.modId)) {
            list.add(new StorageBoxStack.StorageBoxHook());
        }*/
        list.add(new InfChestHook());
        DEFAULT_HOOKS = Collections.unmodifiableList(list);
    }

    public static InsertingHook getInstance() {
        return new InsertingHook(DEFAULT_HOOKS);
    }

    public Optional<Hook> findHookObject(ItemStack maybeHookItem) {
        return hooks.stream().filter(h -> h.isHookItem(maybeHookItem)).findFirst();
    }

    public interface Hook {
        boolean isHookItem(ItemStack stack);

        BigInteger getCount(ItemStack hookItem);

        ItemStack removeAllItems(ItemStack hookItem);

        boolean checkItemAcceptable(ItemStack chestContent, ItemStack hookItem, HolderLookup.Provider provider);
    }

    private static final class InfChestHook implements Hook {
        @Override
        public boolean isHookItem(ItemStack stack) {
            return stack.getItem() == InfChest.accessor.CHEST().itemBlock;
        }

        @Override
        public BigInteger getCount(ItemStack hookItem) {
            var tag = hookItem.get(DataComponents.BLOCK_ENTITY_DATA);
            if (tag == null) {
                return BigInteger.ZERO;
            }

            String itemCount = tag.copyTag().getString(TileInfChest.NBT_COUNT);
            if (itemCount.isEmpty())
                return BigInteger.ZERO;
            else {
                try {
                    return (new BigDecimal(itemCount).toBigIntegerExact()).multiply(BigInteger.valueOf(Math.max(hookItem.getCount(), 1)));
                } catch (NumberFormatException | ArithmeticException e) {
                    InfChest.LOGGER.error("Invalid item count.", e);
                    return BigInteger.ZERO;
                }
            }
        }

        @Override
        public ItemStack removeAllItems(ItemStack hookItem) {
            // Set empty tag to remove tag.
            BlockItem.setBlockEntityData(hookItem, InfChest.accessor.INF_CHEST_TYPE(), new CompoundTag());
            return hookItem;
        }

        @Override
        public boolean checkItemAcceptable(ItemStack chestContent, ItemStack hookItem, HolderLookup.Provider provider) {
            var tag = hookItem.get(DataComponents.BLOCK_ENTITY_DATA);
            if (tag == null) {
                return false;
            }
            ItemStack holding = ItemStack.parseOptional(provider, tag.copyTag().getCompound(TileInfChest.NBT_ITEM));
            return ItemStack.isSameItemSameComponents(chestContent, holding);
        }

        private static ItemStack getSecondItem(CompoundTag nbt, HolderLookup.Provider provider) {
            NonNullList<ItemStack> list = NonNullList.withSize(2, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(nbt, list, provider);
            return list.get(1);
        }
    }
}
