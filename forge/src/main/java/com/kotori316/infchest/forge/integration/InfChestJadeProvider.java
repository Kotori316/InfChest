package com.kotori316.infchest.forge.integration;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.integration.CommonTooltipPart;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

public class InfChestJadeProvider implements IServerDataProvider<BlockAccessor>, IBlockComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof TileInfChest chest) {
            CommonTooltipPart.getTooltipBodyParts(accessor.getServerData(), chest)
                    .forEach(tooltip::add);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        CommonTooltipPart.addTileData(tag, accessor.getBlockEntity());
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(InfChest.modID, "jade_plugin");
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.BODY;
    }
}
