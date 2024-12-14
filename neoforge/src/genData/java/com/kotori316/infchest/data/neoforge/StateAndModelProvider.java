package com.kotori316.infchest.data.neoforge;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.blocks.BlockDeque;
import com.kotori316.infchest.common.blocks.BlockInfChest;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class StateAndModelProvider extends BlockStateProvider {
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final Map<ResourceLocation, ClientItem> clientItemMap = new HashMap<>();

    public StateAndModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, InfChest.modID, exFileHelper);
        this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
    }

    @Override
    protected void registerStatesAndModels() {
        this.simpleBlockWithItem(InfChest.accessor.CHEST(), models().cubeAll(BlockInfChest.name, modLoc(ModelProvider.BLOCK_FOLDER + "/" + "if")));
        this.simpleBlockWithItem(InfChest.accessor.DEQUE(), models().cubeAll(BlockDeque.name, modLoc(ModelProvider.BLOCK_FOLDER + "/" + BlockDeque.name)));
    }

    @Override
    public void simpleBlockItem(Block block, ModelFile model) {
        super.simpleBlockItem(block, model);
        var key = BuiltInRegistries.BLOCK.getKey(block);
        var unbaked = ItemModelUtils.plainModel(TextureMapping.getItemTexture(block.asItem()));
        clientItemMap.put(
            key,
            new ClientItem(unbaked, ClientItem.Properties.DEFAULT)
        );
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var parent = super.run(cache);
        return parent.thenCompose(v ->
            DataProvider.saveAll(cache, ClientItem.CODEC, itemInfoPathProvider, clientItemMap)
        );
    }
}
