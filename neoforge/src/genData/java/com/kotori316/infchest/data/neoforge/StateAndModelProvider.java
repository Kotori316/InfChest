package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class StateAndModelProvider extends ModelProvider {

    public StateAndModelProvider(PackOutput output) {
        super(output, InfChest.modID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.blockStateOutput.accept(
            BlockModelGenerators.createSimpleBlock(InfChest.accessor.CHEST(), TexturedModel.CUBE.updateTexture(t -> t.put(TextureSlot.ALL, ResourceLocation.fromNamespaceAndPath(InfChest.modID, "block/if"))).create(InfChest.accessor.CHEST(), blockModels.modelOutput))
        );
        blockModels.createTrivialCube(InfChest.accessor.DEQUE());
    }
}
