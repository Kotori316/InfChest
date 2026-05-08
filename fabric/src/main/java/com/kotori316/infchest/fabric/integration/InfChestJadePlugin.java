package com.kotori316.infchest.fabric.integration;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.integration.CommonTooltipPart;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin(InfChest.modID)
public class InfChestJadePlugin implements IWailaPlugin {
    @Override
    public void register(@NotNull IWailaCommonRegistration registration) {
        IWailaPlugin.super.register(registration);
        registration.registerBlockDataProvider(new InfChestBlockDataProvider(), TileInfChest.class);
    }

    @Override
    public void registerClient(@NotNull IWailaClientRegistration registration) {
        IWailaPlugin.super.registerClient(registration);
        registration.registerBlockComponent(new InfChestBlockComponentProvider(), BlockInfChest.class);
    }

    private static final class InfChestBlockDataProvider implements IServerDataProvider<BlockAccessor> {

        @Override
        public void appendServerData(@NotNull CompoundTag compoundTag, BlockAccessor blockAccessor) {
            CommonTooltipPart.addTileData(compoundTag, blockAccessor.getBlockEntity());
        }

        @NotNull
        @Override
        public Identifier getUid() {
            return Identifier.fromNamespaceAndPath(InfChest.modID, "jade_plugin");
        }

        @Override
        public int getDefaultPriority() {
            return TooltipPosition.BODY;
        }
    }

    private static final class InfChestBlockComponentProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(@NotNull ITooltip iTooltip, @NotNull BlockAccessor blockAccessor, @NotNull IPluginConfig iPluginConfig) {
            if (blockAccessor.getBlockEntity() instanceof TileInfChest chest) {
                CommonTooltipPart.getTooltipBodyParts(blockAccessor.getServerData(), chest)
                    .forEach(iTooltip::add);
            }
        }

        @NotNull
        @Override
        public Identifier getUid() {
            return Identifier.fromNamespaceAndPath(InfChest.modID, "jade_plugin");
        }
    }
}
