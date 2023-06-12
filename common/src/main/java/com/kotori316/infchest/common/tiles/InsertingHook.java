package com.kotori316.infchest.common.tiles;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.integration.StorageBoxStack;
import net.minecraft.core.NonNullList;
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
        if (InfChest.accessor.isModLoaded(StorageBoxStack.modId)) {
            list.add(new StorageBoxStack.StorageBoxHook());
        }
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

        boolean checkItemAcceptable(ItemStack chestContent, ItemStack hookItem);
    }

    private static final class InfChestHook implements Hook {
        @Override
        public boolean isHookItem(ItemStack stack) {
            return stack.getItem() == InfChest.accessor.CHEST().itemBlock;
        }

        @Override
        public BigInteger getCount(ItemStack hookItem) {
            var tag = BlockItem.getBlockEntityData(hookItem);
            if (tag == null) {
                return BigInteger.ZERO;
            }

            ItemStack secondStack = getSecondItem(tag);
            ItemStack holding = ItemStack.of(tag.getCompound(TileInfChest.NBT_ITEM));
            holding.setCount(1);
            BigInteger second;
            if (ItemStack.isSameItemSameTags(secondStack, holding))
                second = BigInteger.valueOf(secondStack.getCount());
            else
                second = BigInteger.ZERO;
            String itemCount = tag.getString(TileInfChest.NBT_COUNT);
            if (itemCount.isEmpty())
                return BigInteger.ZERO;
            else
                try {
                    return (new BigDecimal(itemCount).toBigIntegerExact().add(second)).multiply(BigInteger.valueOf(Math.max(hookItem.getCount(), 1)));
                } catch (NumberFormatException | ArithmeticException e) {
                    InfChest.LOGGER.error("Invalid item count.", e);
                    return BigInteger.ZERO;
                }
        }

        @Override
        public ItemStack removeAllItems(ItemStack hookItem) {
            // Set empty tag to remove tag.
            BlockItem.setBlockEntityData(hookItem, InfChest.accessor.INF_CHEST_TYPE(), new CompoundTag());
            return hookItem;
        }

        @Override
        public boolean checkItemAcceptable(ItemStack chestContent, ItemStack hookItem) {
            var tag = BlockItem.getBlockEntityData(hookItem);
            if (tag == null) {
                return false;
            }
            ItemStack holding = ItemStack.of(tag.getCompound(TileInfChest.NBT_ITEM));
            return ItemStack.isSameItemSameTags(chestContent, holding);
        }

        private static ItemStack getSecondItem(CompoundTag nbt) {
            NonNullList<ItemStack> list = NonNullList.withSize(2, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(nbt, list);
            return list.get(1);
        }
    }
}
