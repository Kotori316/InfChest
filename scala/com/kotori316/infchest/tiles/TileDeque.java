package com.kotori316.infchest.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.ItemDamage;
import com.kotori316.infchest.blocks.BlockDeque;

public class TileDeque extends BlockEntity implements HasInv {

    public static final String NBT_ITEMS = "items";
    public static final int MAX_COUNT = 1000000; // 1 million
    LinkedList<ItemStack> inventory = new LinkedList<>();

    public TileDeque(BlockPos pos, BlockState state) {
        super(InfChest.Register.DEQUE_TYPE, pos, state);
    }

    @Override
    public void readNbt(NbtCompound compound) {
        super.readNbt(compound);
        inventory = compound.getList(NBT_ITEMS, 10).stream()
            .map(NbtCompound.class::cast)
            .map(ItemStack::fromNbt)
            .filter(InfChest.STACK_NON_EMPTY)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {
        NbtList list1 = inventory.stream()
            .filter(InfChest.STACK_NON_EMPTY)
            .map(stack -> stack.writeNbt(new NbtCompound()))
            .collect(Collectors.toCollection(NbtList::new));
        compound.put(NBT_ITEMS, list1);
        return super.writeNbt(compound);
    }

    public Text getName() {
        String name = InfChest.modID + ":tile." + BlockDeque.name;
        return new TranslatableText(name);
    }

    @Override
    public int size() {
        return Math.min(inventory.size() + 2, MAX_COUNT);
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int index) {
        if (index == 0 || index > inventory.size()) {
            return ItemStack.EMPTY; // Prevent hopper from stopping its work.
        }
        return inventory.get(index - 1);
    }

    @Override
    public ItemStack removeStack(int index, int count) {
        return Inventories.splitStack(inventory, index - 1, count); // range check is done inside the method.
    }

    @Override
    public ItemStack removeStack(int index) {
        return Inventories.removeStack(inventory, index - 1); // range check is done inside the method.
    }

    @Override
    public void setStack(int index, ItemStack stack) {
        if (0 < index && index <= inventory.size())
            inventory.set(index - 1, stack);
        else if (index == 0 && size() < TileDeque.MAX_COUNT)
            inventory.add(stack);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        inventory = inventory.stream().filter(InfChest.STACK_NON_EMPTY).collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public void clear() {
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
