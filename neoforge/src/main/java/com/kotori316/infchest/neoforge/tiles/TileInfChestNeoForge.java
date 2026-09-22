package com.kotori316.infchest.neoforge.tiles;

import com.kotori316.infchest.common.packets.ItemCountMessage;
import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.neoforge.packets.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class TileInfChestNeoForge extends TileInfChest {
    private final InfItemHandler handler = new InfItemHandler(this);

    public TileInfChestNeoForge(BlockPos pos, BlockState state) {
        super(pos, state);
        addUpdate(() -> PacketHandler.sendToPoint(new ItemCountMessage(this, this.totalCount()), getLevel()));
    }

    @Override
    protected void addStack(ItemStack insert) {
        super.addStack(insert);
    }

    public static final ICapabilityProvider<TileInfChestNeoForge, Direction, ResourceHandler<ItemResource>> CAPABILITY_PROVIDER = (tile, _) -> tile.handler;

}
