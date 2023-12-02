package com.kotori316.infchest.neoforge.integration;

import com.kotori316.infchest.common.integration.CommonTooltipPart;
import com.kotori316.infchest.common.tiles.TileInfChest;
import mcp.mobius.waila.api.*;

public final class InfChestWthitProvider implements IDataProvider<TileInfChest>, IBlockComponentProvider {

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TileInfChest) {
            CommonTooltipPart.getTooltipBodyParts(accessor.getData().raw())
                    .forEach(tooltip::addLine);
        }
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<TileInfChest> accessor, IPluginConfig config) {
        CommonTooltipPart.addTileData(data.raw(), accessor.getTarget());
    }
}
