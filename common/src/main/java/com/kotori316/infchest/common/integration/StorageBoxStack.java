package com.kotori316.infchest.common.integration;

import java.math.BigInteger;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.InsertingHook;
import com.kotori316.infchest.common.tiles.TileInfChest;

/**
 * Integration class of mod <a href="https://www.curseforge.com/minecraft/mc-mods/break-all-of-the-same-block-and-more/files/2877188">"StorageBox"</a>.
 */
@SuppressWarnings("SpellCheckingInspection")
public class StorageBoxStack {
    public static final String modId = "storagebox";
    private static final String KEYSIZE = "StorageSize";
    private static final String KEY_ITEM_DATA = "StorageItemData";

    private static class Holder {
        public static final boolean modLoaded = InfChest.accessor.isModLoaded(modId);
    }

    public static boolean isStorageBox(ItemStack maybeBox) {
        if (!Holder.modLoaded) return false;
        Item item = maybeBox.getItem();
        return item == BuiltInRegistries.ITEM.get(new ResourceLocation(modId, "storagebox"));
    }

    public static boolean checkHoldingItem(ItemStack holding, ItemStack maybeBox) {
        if (!Holder.modLoaded || holding.isEmpty()) return false;
        if (isStorageBox(maybeBox)) {
            ItemStack inBox = getItem(maybeBox);
            return !inBox.isEmpty() && ItemStack.isSame(holding, inBox) && ItemStack.tagMatches(holding, inBox);
        } else {
            return false;
        }
    }

    public static BigInteger getCount(ItemStack maybeBox) {
        if (isStorageBox(maybeBox)) {
            return Optional.ofNullable(maybeBox.getTag())
                .map(n -> n.getInt(KEYSIZE))
                .filter(i -> i > 0)
                .map(BigInteger::valueOf)
                .orElse(BigInteger.ZERO);
        } else {
            return BigInteger.ZERO;
        }
    }

    /**
     * @param maybeBox the storage box
     * @return copied stack that has no item in storage.
     */
    public static ItemStack removeAllItems(ItemStack maybeBox) {
        if (isStorageBox(maybeBox)) {
            ItemStack box = maybeBox.copy();
            box.removeTagKey(KEY_ITEM_DATA);
            box.getOrCreateTag(); // Storage box should always have tag.
            return box;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static boolean moveToStorage(Level worldIn, BlockPos pos, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isStorageBox(stack)) {
            if (worldIn.getBlockEntity(pos) instanceof TileInfChest chest) {
                if (checkHoldingItem(chest.getStack(1), stack)) {
                    // flag that checks if the item in second slot of inf check can be inserted to storage box.
                    boolean flag = checkHoldingItem(chest.getItem(1), stack);
                    BigInteger need = chest.itemCount().min(BigInteger.valueOf(2_000_000_000L).subtract(getCount(stack)));
                    if (need.compareTo(BigInteger.ZERO) > 0) { // need > 0
                        chest.decrStack(need);
                        var nbt = stack.getOrCreateTag();
                        nbt.putInt(KEYSIZE, need.add(getCount(stack)).add(flag ? BigInteger.valueOf(chest.getItem(1).getCount()) : BigInteger.ZERO).intValueExact());
                        stack.setTag(nbt);
                        if (flag) {
                            chest.setItem(1, ItemStack.EMPTY);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static ItemStack getItem(ItemStack box) {
        if (!Holder.modLoaded) return ItemStack.EMPTY;
        return Optional.ofNullable(box.getTagElement(KEY_ITEM_DATA))
            .map(ItemStack::of)
            .flatMap(s ->
                Optional.ofNullable(box.getTag())
                    .map(n -> n.getInt(KEYSIZE))
                    .filter(i -> i > 0)
                    .map(i -> {
                        s.setCount(i);
                        return s;
                    }))
            .filter(s -> !s.isEmpty())
            .orElse(ItemStack.EMPTY);
    }

    public static class StorageBoxHook implements InsertingHook.Hook {

        @Override
        public boolean isHookItem(ItemStack stack) {
            return StorageBoxStack.isStorageBox(stack);
        }

        @Override
        public BigInteger getCount(ItemStack hookItem) {
            return StorageBoxStack.getCount(hookItem);
        }

        @Override
        public ItemStack removeAllItems(ItemStack hookItem) {
            return StorageBoxStack.removeAllItems(hookItem);
        }

        @Override
        public boolean checkItemAcceptable(ItemStack chestContent, ItemStack hookItem) {
            return StorageBoxStack.checkHoldingItem(chestContent, hookItem);
        }
    }
}
