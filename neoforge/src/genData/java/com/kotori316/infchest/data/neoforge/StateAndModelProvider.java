package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.blocks.BlockDeque;
import com.kotori316.infchest.common.blocks.BlockInfChest;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class StateAndModelProvider extends BlockStateProvider {
    public StateAndModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, InfChest.modID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        this.simpleBlockWithItem(InfChest.accessor.CHEST(), models().cubeAll(BlockInfChest.name, modLoc(ModelProvider.BLOCK_FOLDER + "/" + "if")));
        this.simpleBlockWithItem(InfChest.accessor.DEQUE(), models().cubeAll(BlockDeque.name, modLoc(ModelProvider.BLOCK_FOLDER + "/" + BlockDeque.name)));
    }
}
