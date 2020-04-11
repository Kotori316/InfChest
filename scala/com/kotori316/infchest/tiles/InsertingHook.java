package com.kotori316.infchest.tiles;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Loader;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.integration.StorageBoxStack;

public class InsertingHook {
    private final List<Hook> hooks;
    private static final List<Hook> DEFAULT_HOOKS;

    static {
        List<Hook> list = new ArrayList<>();
        // Storage Box
        if (Loader.isModLoaded(StorageBoxStack.modId)) {
            list.add(new StorageBoxStack.StorageBoxHook());
        }
        list.add(new InfChestHook());
        DEFAULT_HOOKS = Collections.unmodifiableList(list);
    }

    public static InsertingHook getInstance() {
        return new InsertingHook(DEFAULT_HOOKS);
    }

    public InsertingHook(List<Hook> hooks) {
        this.hooks = hooks;
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
            return stack.getItem() == InfChest.CHEST.itemBlock;
        }

        @Override
        public BigInteger getCount(ItemStack hookItem) {
            NBTTagCompound tag = hookItem.getSubCompound(TileInfChest.NBT_BLOCK_TAG);
            if (tag == null) {
                return BigInteger.ZERO;
            }

            ItemStack secondStack = getSecondItem(tag);
            ItemStack holding = new ItemStack(tag.getCompoundTag(TileInfChest.NBT_ITEM));
            holding.setCount(1);
            BigInteger second;
            if (ItemStack.areItemsEqual(secondStack, holding) && ItemStack.areItemStackTagsEqual(secondStack, holding))
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
            hookItem.removeSubCompound(TileInfChest.NBT_BLOCK_TAG);
            Optional.ofNullable(hookItem.getTagCompound())
                .filter(NBTTagCompound::hasNoTags)
                .ifPresent(n -> hookItem.setTagCompound(null));
            return hookItem;
        }

        @Override
        public boolean checkItemAcceptable(ItemStack chestContent, ItemStack hookItem) {
            NBTTagCompound tag = hookItem.getSubCompound(TileInfChest.NBT_BLOCK_TAG);
            if (tag == null) {
                return false;
            }
            ItemStack holding = new ItemStack(tag.getCompoundTag(TileInfChest.NBT_ITEM));
            return ItemStack.areItemsEqual(chestContent, holding) && ItemStack.areItemStackTagsEqual(chestContent, holding);
        }

        private static ItemStack getSecondItem(NBTTagCompound nbt) {
            NonNullList<ItemStack> list = NonNullList.withSize(2, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(nbt, list);
            return list.get(1);
        }
    }
}
