package com.kotori316.infchest.forge.integration;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.kotori316.infchest.common.integration.CommonTooltipPart;
import com.kotori316.infchest.common.tiles.TileInfChest;

public final class InfChestWthitProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TileInfChest) {
            CommonTooltipPart.getTooltipBodyParts(accessor.getServerData())
                .forEach(tooltip::addLine);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, IServerAccessor<BlockEntity> accessor, IPluginConfig config) {
        CommonTooltipPart.addTileData(tag, accessor.getTarget());
    }
}
