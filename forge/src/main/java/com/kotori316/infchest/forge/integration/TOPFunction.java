package com.kotori316.infchest.forge.integration;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import mcjty.theoneprobe.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Function;

public class TOPFunction implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe iTheOneProbe) {
        iTheOneProbe.registerProvider(new TOPProvider());
        return null;
    }

    private static class TOPProvider implements IProbeInfoProvider {

        @Override
        public ResourceLocation getID() {
            return new ResourceLocation(InfChest.modID, "top_chest");
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player,
                                 Level world, BlockState blockState, IProbeHitData data) {
            if (world.getBlockEntity(data.getPos()) instanceof TileInfChest chest) {
                final ItemStack stack = chest.getHoldingWithOneCount();
                if (!stack.isEmpty()) {
                    // TODO wrong parchment mapping?
                    /*Arrays.asList(
                        stack.getDisplayName(),
                        Component.literal(chest.totalCount().toString())
                    ).forEach(probeInfo::text);*/
                }
            }
        }
    }
}
