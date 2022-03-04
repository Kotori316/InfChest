package com.kotori316.infchest;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record ItemDamage(Item item, @Nullable CompoundTag compound) {

    public ItemDamage(ItemStack stack) {
        this(stack.getItem(), stack.getTag());
    }

    public ItemStack toStack(int count) {
        ItemStack stack = new ItemStack(item, count);
        stack.setTag(compound);
        return stack;
    }

    public Stream<ItemStack> toStacks(long count) {
        long len = count / Integer.MAX_VALUE;
        int rest = (int) (count - len * Integer.MAX_VALUE);
        return IntStream.concat(IntStream.generate(() -> Integer.MAX_VALUE).limit(len), IntStream.of(rest))
            .mapToObj(this::toStack);
    }
}
