package com.kotori316.infchest.tiles;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;

import com.kotori316.infchest.InfChest;

public record InsertingHook(List<Hook> hooks) {
    private static final List<Hook> DEFAULT_HOOKS;

    static {
        DEFAULT_HOOKS = List.of(new InfChestHook());
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
            return stack.getItem() == InfChest.Register.CHEST.itemBlock;
        }

        @Override
        public BigInteger getCount(ItemStack hookItem) {
            NbtCompound tag = hookItem.getSubTag(TileInfChest.NBT_BLOCK_TAG);
            if (tag == null) {
                return BigInteger.ZERO;
            }

            ItemStack secondStack = getSecondItem(tag);
            ItemStack holding = ItemStack.fromNbt(tag.getCompound(TileInfChest.NBT_ITEM));
            holding.setCount(1);
            BigInteger second;
            if (ItemStack.areItemsEqual(secondStack, holding) && ItemStack.areTagsEqual(secondStack, holding))
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
            hookItem.removeSubTag(TileInfChest.NBT_BLOCK_TAG);
            return hookItem;
        }

        @Override
        public boolean checkItemAcceptable(ItemStack chestContent, ItemStack hookItem) {
            NbtCompound tag = hookItem.getSubTag(TileInfChest.NBT_BLOCK_TAG);
            if (tag == null) {
                return false;
            }
            ItemStack holding = ItemStack.fromNbt(tag.getCompound(TileInfChest.NBT_ITEM));
            return ItemStack.areItemsEqual(chestContent, holding) && ItemStack.areTagsEqual(chestContent, holding);
        }

        private static ItemStack getSecondItem(NbtCompound nbt) {
            DefaultedList<ItemStack> list = DefaultedList.ofSize(2, ItemStack.EMPTY);
            Inventories.readNbt(nbt, list);
            return list.get(1);
        }
    }
}
