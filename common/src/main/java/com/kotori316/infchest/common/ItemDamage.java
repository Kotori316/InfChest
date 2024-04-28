package com.kotori316.infchest.common;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;
import java.util.stream.Stream;

public record ItemDamage(Item item, @NotNull DataComponentPatch component) {

    public ItemDamage(ItemStack stack) {
        this(stack.getItem(), stack.getComponentsPatch());
    }

    public ItemStack toStack(int count) {
        return new ItemStack(Holder.direct(item), count, component);
    }

    public Stream<ItemStack> toStacks(long count) {
        long len = count / Integer.MAX_VALUE;
        int rest = (int) (count - len * Integer.MAX_VALUE);
        return IntStream.concat(IntStream.generate(() -> Integer.MAX_VALUE).limit(len), IntStream.of(rest))
            .mapToObj(this::toStack);
    }
}
