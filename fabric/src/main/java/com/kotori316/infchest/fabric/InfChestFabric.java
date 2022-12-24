package com.kotori316.infchest.fabric;

import com.mojang.datafixers.DSL;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.blocks.BlockDeque;
import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.blocks.ContentInfChest;
import com.kotori316.infchest.common.guis.ContainerInfChest;
import com.kotori316.infchest.common.tiles.TileDeque;
import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.fabric.blocks.BlockInfChestFabric;
import com.kotori316.infchest.fabric.tiles.InfChestStorage;
import com.kotori316.infchest.fabric.tiles.TileInfChestFabric;

import static com.kotori316.infchest.common.InfChest.modID;

public class InfChestFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(modID, BlockInfChestFabric.name), Register.CHEST);
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(modID, BlockDeque.name), Register.DEQUE);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(modID, BlockInfChestFabric.name), Register.CHEST.itemBlock);
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(modID, BlockDeque.name), Register.DEQUE.itemBlock);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(modID, "tile." + BlockInfChestFabric.name), Register.INF_CHEST_TYPE);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(modID, "tile." + BlockDeque.name), Register.DEQUE_TYPE);
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ContentInfChest.LOCATION, Register.CHEST_FUNCTION);
        Registry.register(BuiltInRegistries.MENU, new ResourceLocation(TileInfChestFabric.GUI_ID), Register.INF_CHEST_CONTAINER_TYPE);

        InfChestStorage.register();
    }

    public static class Register implements InfChest.TypeAccessor {
        public static final BlockInfChestFabric CHEST = new BlockInfChestFabric();
        public static final BlockDeque DEQUE = new BlockDeque();
        public static final BlockEntityType<TileInfChestFabric> INF_CHEST_TYPE = FabricBlockEntityTypeBuilder.create(TileInfChestFabric::new, CHEST).build(DSL.emptyPartType());
        public static final BlockEntityType<TileDeque> DEQUE_TYPE = FabricBlockEntityTypeBuilder.create(TileDeque::new, DEQUE).build(DSL.emptyPartType());
        public static final ExtendedScreenHandlerType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE = new ExtendedScreenHandlerType<>(ContainerInfChest::create);
        public static final LootItemFunctionType CHEST_FUNCTION = new LootItemFunctionType(new ContentInfChest.Serializer());

        @Override
        public BlockEntityType<? extends TileInfChest> INF_CHEST_TYPE() {
            return INF_CHEST_TYPE;
        }

        @Override
        public BlockEntityType<? extends TileDeque> DEQUE_TYPE() {
            return DEQUE_TYPE;
        }

        @Override
        public BlockInfChest CHEST() {
            return CHEST;
        }

        @Override
        public LootItemFunctionType CHEST_FUNCTION() {
            return CHEST_FUNCTION;
        }

        @Override
        public MenuType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE() {
            return INF_CHEST_CONTAINER_TYPE;
        }

        @Override
        public boolean isModLoaded(String modId) {
            return FabricLoader.getInstance().isModLoaded(modId);
        }

        static {
            InfChest.accessor = new Register();
            ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> {
                    entries.accept(CHEST);
                    entries.accept(DEQUE);
                });
        }
    }
}
