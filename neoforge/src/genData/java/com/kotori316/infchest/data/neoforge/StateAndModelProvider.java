package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public final class StateAndModelProvider extends ModelProvider {

    public StateAndModelProvider(PackOutput output) {
        super(output, InfChest.modID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        var variant = BlockModelGenerators.plainVariant(TexturedModel.CUBE.updateTexture(t -> t.put(TextureSlot.ALL, Identifier.fromNamespaceAndPath(InfChest.modID, "block/if"))).create(InfChest.accessor.CHEST(), blockModels.modelOutput));
        blockModels.blockStateOutput.accept(
            BlockModelGenerators.createSimpleBlock(InfChest.accessor.CHEST(), variant)
        );
        blockModels.createTrivialCube(InfChest.accessor.DEQUE());
    }
}
