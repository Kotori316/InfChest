package com.kotori316.infchest.integration;
/*
import java.util.Arrays;
import java.util.function.Function;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

public class TOPFunction implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe iTheOneProbe) {
        iTheOneProbe.registerProvider(new TOPProvider());
        return null;
    }

    private static class TOPProvider implements IProbeInfoProvider {

        @Override
        public String getID() {
            return InfChest.modID + ":top_chest";
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player,
                                 World world, BlockState blockState, IProbeHitData data) {
            final TileEntity entity = world.getTileEntity(data.getBlockPos());
            if (entity instanceof TileInfChest) {
                TileInfChest chest = (TileInfChest) entity;
                final ItemStack stack = chest.getStack(1);
                if (!stack.isEmpty()) {
                    Arrays.asList(
                        stack.getDisplayName(),
                        new StringTextComponent(chest.itemCount().toString())
                    ).forEach(probeInfo::text);
                }
            }
        }
    }
}
*/