package com.kotori316.infchest.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.ItemDamage;

public class TileDeque extends BlockEntity implements HasInv {

    public static final String NBT_ITEMS = "items";
    public static final int MAX_COUNT = 1000000; // 1 million
    LinkedList<ItemStack> inventory = new LinkedList<>();

    public TileDeque(BlockPos pos, BlockState state) {
        super(InfChest.Register.DEQUE_TYPE, pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        inventory = compound.getList(NBT_ITEMS, 10).stream()
            .map(CompoundTag.class::cast)
            .map(ItemStack::of)
            .filter(Predicate.not(ItemStack::isEmpty))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        ListTag list1 = inventory.stream()
            .filter(Predicate.not(ItemStack::isEmpty))
            .map(stack -> stack.save(new CompoundTag()))
            .collect(Collectors.toCollection(ListTag::new));
        compound.put(NBT_ITEMS, list1);
        super.saveAdditional(compound);
    }

    @Override
    public int getContainerSize() {
        return Math.min(inventory.size() + 2, MAX_COUNT);
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
        else if (index == 0 && getContainerSize() < TileDeque.MAX_COUNT)
            inventory.add(stack);
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
