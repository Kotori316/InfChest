package com.kotori316.infchest.fabric.integration;

import com.kotori316.infchest.common.integration.CommonTooltipPart;
import com.kotori316.infchest.common.tiles.TileInfChest;
import mcp.mobius.waila.api.*;

public class InfChestWthitProvider implements IDataProvider<TileInfChest>, IBlockComponentProvider {

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TileInfChest chest) {
            CommonTooltipPart.getTooltipBodyParts(accessor.getData().raw(), chest)
                .forEach(tooltip::addLine);
        }
    }

    @Override
    public void appendData(IDataWriter data, IServerAccessor<TileInfChest> accessor, IPluginConfig config) {
        CommonTooltipPart.addTileData(data.raw(), accessor.getTarget());
    }

}
