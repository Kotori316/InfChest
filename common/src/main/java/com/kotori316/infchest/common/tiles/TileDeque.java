package com.kotori316.infchest.common.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.ItemDamage;

public class TileDeque extends BlockEntity implements HasInv {

    public static final String NBT_ITEMS = "items";
    public static final int MAX_COUNT = 1000000; // 1 million
    protected LinkedList<ItemStack> inventory = new LinkedList<>();

    public TileDeque(BlockPos pos, BlockState state) {
        super(InfChest.accessor.DEQUE_TYPE(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        inventory = compound.getList(NBT_ITEMS, Tag.TAG_COMPOUND).stream()
            .map(CompoundTag.class::cast)
            .map(ItemStack::of)
            .filter(Predicate.not(ItemStack::isEmpty))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        var list1 = inventory.stream()
            .filter(Predicate.not(ItemStack::isEmpty))
            .map(i -> i.save(new CompoundTag()))
            .collect(Collectors.toCollection(ListTag::new));
        compound.put(NBT_ITEMS, list1);
        super.saveAdditional(compound);
    }

    @Override
    public int getContainerSize() {
        return Math.min(inventory.size() + 1, MAX_COUNT);
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int index) {
        if (index == 0 || index > inventory.size()) {
            return ItemStack.EMPTY; // Prevent hopper from stopping its work.
        }
        return inventory.get(index - 1);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(inventory, index - 1, count); // range check is done inside the method.
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(inventory, index - 1); // range check is done inside the method.
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (0 < index && index <= inventory.size())
            inventory.set(index - 1, stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        inventory = inventory.stream().filter(Predicate.not(ItemStack::isEmpty)).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    public List<ItemStack> itemsList() {
        return inventory.stream()
            .collect(Collectors.groupingBy(ItemDamage::new, Collectors.summingLong(ItemStack::getCount)))
            .entrySet()
            .stream()
            .flatMap(e -> e.getKey().toStacks(e.getValue()))
            .collect(Collectors.toList());
    }

}
