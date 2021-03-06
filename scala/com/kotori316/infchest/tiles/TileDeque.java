package com.kotori316.infchest.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.ItemDamage;
import com.kotori316.infchest.blocks.BlockDeque;

public class TileDeque extends TileEntity implements HasInv {

    public static final String NBT_ITEMS = "items";
    public static final int MAX_COUNT = 1000000; // 1 million
    LinkedList<ItemStack> inventory = new LinkedList<>();
    private final IItemHandler handler;

    public TileDeque() {
        super(InfChest.Register.DEQUE_TYPE);
        handler = new DequeItemHandler(this);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);
        inventory = compound.getList(NBT_ITEMS, Constants.NBT.TAG_COMPOUND).stream()
            .map(CompoundNBT.class::cast)
            .map(ItemStack::read)
            .filter(InfChest.STACK_NON_EMPTY)
            .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list1 = inventory.stream()
            .filter(InfChest.STACK_NON_EMPTY)
            .map(ItemStack::serializeNBT)
            .collect(Collectors.toCollection(ListNBT::new));
        compound.put(NBT_ITEMS, list1);
        return super.write(compound);
    }

    public ITextComponent getName() {
        String name = InfChest.modID + ":tile." + BlockDeque.name;
        return new TranslationTextComponent(name);
    }

    @Override
    public int getSizeInventory() {
        return Math.min(inventory.size() + 1, MAX_COUNT);
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index == 0 || index > inventory.size()) {
            return ItemStack.EMPTY; // Prevent hopper from stopping its work.
        }
        return inventory.get(index - 1);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(inventory, index - 1, count); // range check is done inside the method.
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(inventory, index - 1); // range check is done inside the method.
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (0 < index && index <= inventory.size())
            inventory.set(index - 1, stack);
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() -> handler));
        }
        return super.getCapability(cap, side);
    }

}
