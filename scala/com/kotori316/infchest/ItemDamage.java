package com.kotori316.infchest;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class ItemDamage {
    private final Item item;
    private final int damage;
    private final NBTTagCompound compound;

    public ItemDamage(ItemStack stack) {
        item = stack.getItem();
        damage = stack.getItemDamage();
        compound = stack.getTagCompound();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDamage that = (ItemDamage) o;
        return damage == that.damage &&
            item.equals(that.item) &&
            Objects.equals(compound, that.compound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, damage, compound);
    }

    public ItemStack toStack(int count) {
        ItemStack stack = new ItemStack(item, count, damage);
        stack.setTagCompound(compound);
        return stack;
    }

    public Stream<ItemStack> toStacks(long count) {
        long len = count / Integer.MAX_VALUE;
        int rest = (int) (count - len * Integer.MAX_VALUE);
        return IntStream.concat(IntStream.generate(() -> Integer.MAX_VALUE).limit(len), IntStream.of(rest))
            .mapToObj(this::toStack);
    }
}
