package com.kotori316.infchest.integration;

import java.math.BigInteger;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.LiteralText;

import com.kotori316.infchest.tiles.TileInfChest;

public class WailaProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {
    private static final String NBT_ITEM = "waila_item";
    private static final String NBT_COUNT = "waila_count";
    private static final String NBT_OUTPUT = "waila_output";

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        BlockEntity t = accessor.getBlockEntity();
        if (t instanceof TileInfChest) {
            final NbtCompound data = accessor.getServerData();
            final ItemStack stack;
            if (data.contains(NBT_ITEM)) {
                stack = ItemStack.fromNbt(data.getCompound(NBT_ITEM));
            } else {
                stack = ItemStack.fromNbt(data.getCompound(TileInfChest.NBT_ITEM));
            }
            final int outputItem;
            if (data.contains(NBT_OUTPUT)) {
                outputItem = ItemStack.fromNbt(data.getCompound(NBT_OUTPUT)).getCount();
            } else {
                outputItem = data.getList("Items", NbtElement.COMPOUND_TYPE).stream()
                    .map(NbtCompound.class::cast)
                    .filter(e -> e.getByte("Slot") == 1) // Find the output slot.
                    .mapToInt(e -> e.getInt("Count"))
                    .findFirst().orElse(0);
            }
            if (!stack.isEmpty()) {
                final BigInteger integer;
                if (data.contains(NBT_COUNT))
                    integer = new BigInteger(data.getByteArray(NBT_COUNT)).add(BigInteger.valueOf(outputItem));
                else if (data.contains(TileInfChest.NBT_COUNT))
                    integer = new BigInteger(data.getString(TileInfChest.NBT_COUNT)).add(BigInteger.valueOf(outputItem));
                else
                    integer = BigInteger.ZERO;
                tooltip.addLine(stack.getName());
                tooltip.addLine(new LiteralText(integer.toString()));
            }
        }
    }

    @Override
    public void appendServerData(NbtCompound tag, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
        var te = accessor.getTarget();
        if (te instanceof TileInfChest chest) {
            tag.put(NBT_ITEM, chest.getStackWithAmount(1).writeNbt(new NbtCompound())); // Holding item
            tag.putByteArray(NBT_COUNT, chest.itemCount().toByteArray()); // Item count
            tag.put(NBT_OUTPUT, chest.getStack(1).writeNbt(new NbtCompound())); // Item in output slot
        }
    }

}
