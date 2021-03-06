package com.kotori316.infchest.integration;

import java.math.BigInteger;
import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ObjectHolder;

import com.kotori316.infchest.tiles.InsertingHook;
import com.kotori316.infchest.tiles.TileInfChest;

/**
 * Integration class of mod <a href="https://www.curseforge.com/minecraft/mc-mods/break-all-of-the-same-block-and-more/files/2877188">"StorageBox"</a>.
 */
@SuppressWarnings("SpellCheckingInspection")
public class StorageBoxStack {
    public static final String modId = "storagebox";
    private static final String KEYSIZE = "StorageSize";
    private static final String KEY_ITEM_DATA = "StorageItemData";

    @ObjectHolder(modId)
    private static class Holder {
        public static final Item storagebox = new Item(new Item.Properties());
        public static final boolean modLoaded = ModList.get().isLoaded(modId);
    }

    public static boolean isStorageBox(ItemStack maybeBox) {
        if (!Holder.modLoaded) return false;
        Item item = maybeBox.getItem();
        return item == Holder.storagebox;
    }

    public static boolean checkHoldingItem(ItemStack holding, ItemStack maybeBox) {
        if (!Holder.modLoaded || holding.isEmpty()) return false;
        if (isStorageBox(maybeBox)) {
            ItemStack inBox = getItem(maybeBox);
            return !inBox.isEmpty() && ItemStack.areItemsEqual(holding, inBox) && ItemStack.areItemStackTagsEqual(holding, inBox);
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
            box.removeChildTag(KEY_ITEM_DATA);
            box.getOrCreateTag(); // Storage box should always have tag.
            return box;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public static boolean moveToStorage(World worldIn, BlockPos pos, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (isStorageBox(stack)) {
            TileEntity t = worldIn.getTileEntity(pos);
            if (t instanceof TileInfChest) {
                TileInfChest chest = (TileInfChest) t;
                if (checkHoldingItem(chest.getStack(1), stack)) {
                    // flag that checks if the item in second slot of inf check can be inserted to storage box.
                    boolean flag = checkHoldingItem(chest.getStackInSlot(1), stack);
                    BigInteger need = chest.itemCount().min(BigInteger.valueOf(2_000_000_000L).subtract(getCount(stack)));
                    if (need.compareTo(BigInteger.ZERO) > 0) { // need > 0
                        chest.decrStack(need);
                        CompoundNBT nbt = stack.getOrCreateTag();
                        nbt.putInt(KEYSIZE, need.add(getCount(stack)).add(flag ? BigInteger.valueOf(chest.getStackInSlot(1).getCount()) : BigInteger.ZERO).intValueExact());
                        stack.setTag(nbt);
                        if (flag) {
                            chest.setInventorySlotContents(1, ItemStack.EMPTY);
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
        return Optional.ofNullable(box.getChildTag(KEY_ITEM_DATA))
            .map(ItemStack::read)
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
