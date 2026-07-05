package com.kotori316.infchest.common.tiles;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.ItemKey;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.LinkedList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TileDeque extends BlockEntity implements HasInv {

    public static final String NBT_ITEMS = "items";
    public static final int MAX_COUNT = 1000000; // 1 million
    protected LinkedList<ItemStack> inventory = new LinkedList<>();

    public TileDeque(BlockPos pos, BlockState state) {
        super(InfChest.accessor.DEQUE_TYPE(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        inventory = valueInput.listOrEmpty(NBT_ITEMS, ItemStack.OPTIONAL_CODEC)
            .stream()
            .filter(Predicate.not(ItemStack::isEmpty))
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        {
            var list = valueOutput.list(NBT_ITEMS, ItemStack.OPTIONAL_CODEC);
            inventory.stream()
                .filter(Predicate.not(ItemStack::isEmpty))
                .forEach(list::add);
        }
        super.saveAdditional(valueOutput);
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
        if (0 < index && index <= inventory.size()) {
            inventory.set(index - 1, stack);
        } else if (index == 0) {
            inventory.add(stack);
        }
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

    public NonNullList<ItemStack> itemsList() {
        return inventory.stream()
            .collect(Collectors.groupingBy(ItemKey::new, Collectors.summingLong(ItemStack::getCount)))
            .entrySet()
            .stream()
            .flatMap(e -> e.getKey().toStacks(e.getValue()))
            .collect(Collectors.toCollection(NonNullList::create));
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null) {
            Containers.dropContents(level, pos, this.itemsList());
        }
    }
}
