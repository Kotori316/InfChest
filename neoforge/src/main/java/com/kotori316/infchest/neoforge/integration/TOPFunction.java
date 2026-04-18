package com.kotori316.infchest.neoforge.integration;

import mcjty.theoneprobe.api.ITheOneProbe;

import java.util.function.Function;

public class TOPFunction implements Function<ITheOneProbe, Void> {

    @Override
    public Void apply(ITheOneProbe iTheOneProbe) {
        // iTheOneProbe.registerProvider(new TOPProvider());
        return null;
    }

    /*private static class TOPProvider implements IProbeInfoProvider {

        @Override
        public Identifier getID() {
            return Identifier.fromNamespaceAndPath(InfChest.modID, "top_chest");
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player,
                                 Level world, BlockState blockState, IProbeHitData data) {
            if (world.getBlockEntity(data.getPos()) instanceof TileInfChest chest) {
                final ItemStack stack = chest.getHoldingWithOneCount();
                if (!stack.isEmpty()) {
                    Arrays.asList(
                        stack.getDisplayName(),
                        Component.literal(chest.totalCount().toString())
                    ).forEach(probeInfo::text);
                }
            }
        }
    }*/
}
