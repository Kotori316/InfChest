package com.kotori316.infchest.forge.tiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.forge.packets.ItemCountMessage;
import com.kotori316.infchest.forge.packets.PacketHandler;

public final class TileInfChestForge extends TileInfChest {
    private final InfItemHandler handler = new InfItemHandler(this);

    public TileInfChestForge(BlockPos pos, BlockState state) {
        super(pos, state);
        addUpdate(() -> PacketHandler.sendToPoint(new ItemCountMessage(this, this.itemCount())));
    }

    @Override
    protected void addStack(ItemStack insert) {
        super.addStack(insert);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> handler));
        }
        return super.getCapability(cap, side);
    }

}
