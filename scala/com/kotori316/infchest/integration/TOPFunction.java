package com.kotori316.infchest.integration;

import java.util.Arrays;
import java.util.function.Function;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

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
        public ResourceLocation getID() {
            return new ResourceLocation(InfChest.modID, "top_chest");
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player,
                                 Level world, BlockState blockState, IProbeHitData data) {
            if (world.getBlockEntity(data.getPos()) instanceof TileInfChest chest) {
                final ItemStack stack = chest.getStack(1);
                if (!stack.isEmpty()) {
                    Arrays.asList(
                        stack.getDisplayName(),
                        new TextComponent(chest.itemCount().toString())
                    ).forEach(probeInfo::text);
                }
            }
        }
    }
}
