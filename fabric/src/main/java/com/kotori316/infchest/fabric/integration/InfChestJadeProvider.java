package com.kotori316.infchest.fabric.integration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.integration.CommonTooltipPart;
import com.kotori316.infchest.common.tiles.TileInfChest;

enum InfChestJadeProvider implements IServerDataProvider<BlockEntity>, IBlockComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        BlockEntity t = accessor.getBlockEntity();
        if (t instanceof TileInfChest) {
            CommonTooltipPart.getTooltipBodyParts(accessor.getServerData())
                .forEach(tooltip::add);
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer serverPlayer, Level world, BlockEntity te, boolean b) {
        CommonTooltipPart.addTileData(tag, te);
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
