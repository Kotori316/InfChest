package com.kotori316.infchest;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ItemDamage(@NotNull Item item, @Nullable CompoundTag compound) {
    public ItemDamage(ItemStack stack) {
        this(stack.getItem(), stack.getTag() == null ? null : stack.getTag().copy());
    }

    public ItemStack toStack(int count) {
        var stack = new ItemStack(item, count);
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
