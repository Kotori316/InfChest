package com.kotori316.infchest.integration;

import java.math.BigInteger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class InfChestJadeProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {
    private static final String NBT_ITEM = "jade_item";
    private static final String NBT_COUNT = "jade_count";

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity t = accessor.getBlockEntity();
        if (t instanceof TileInfChest) {
            CompoundTag data = accessor.getServerData();
            ItemStack stack;
            if (data.contains(NBT_ITEM)) {
                stack = ItemStack.of(data.getCompound(NBT_ITEM));
            } else {
                stack = ItemStack.of(data.getCompound(TileInfChest.NBT_ITEM));
            }
            if (!stack.isEmpty()) {
                BigInteger integer;
                if (data.contains(NBT_COUNT))
                    integer = new BigInteger(data.getByteArray(NBT_COUNT));
                else if (data.contains(TileInfChest.NBT_COUNT))
                    integer = new BigInteger(data.getString(TileInfChest.NBT_COUNT)).subtract(BigInteger.valueOf(stack.getMaxStackSize()));
                else
                    integer = BigInteger.ZERO;
                tooltip.add(stack.getDisplayName());
                tooltip.add(Component.literal(integer.toString()));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer serverPlayer, Level world, BlockEntity te, boolean b) {
        if (te instanceof TileInfChest chest) {
            tag.put(NBT_ITEM, chest.getHoldingWithOneCount().serializeNBT());
            tag.putByteArray(NBT_COUNT, chest.totalCount().toByteArray());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(InfChest.modID, "jade_plugin");
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.BODY;
    }
}
