package com.kotori316.infchest;

import com.mojang.datafixers.DSL;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.infchest.blocks.BlockDeque;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.blocks.ContentInfChest;
import com.kotori316.infchest.guis.ContainerInfChest;
import com.kotori316.infchest.tiles.TileDeque;
import com.kotori316.infchest.tiles.TileInfChest;

public class InfChest implements ModInitializer {
    public static final String MOD_NAME = "InfChest";
    public static final String modID = "infchest";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockInfChest.name), Register.CHEST);
        Registry.register(Registry.BLOCK, new Identifier(modID, BlockDeque.name), Register.DEQUE);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockInfChest.name), Register.CHEST.itemBlock);
        Registry.register(Registry.ITEM, new Identifier(modID, BlockDeque.name), Register.DEQUE.itemBlock);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, "tile." + BlockInfChest.name), Register.INF_CHEST_TYPE);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(modID, "tile." + BlockDeque.name), Register.DEQUE_TYPE);
        Registry.register(Registry.LOOT_FUNCTION_TYPE, ContentInfChest.LOCATION, Register.CHEST_FUNCTION);
    }

    public static class Register {
        public static final BlockInfChest CHEST = new BlockInfChest();
        public static final BlockDeque DEQUE = new BlockDeque();
        public static final BlockEntityType<TileInfChest> INF_CHEST_TYPE = FabricBlockEntityTypeBuilder.create(TileInfChest::new, CHEST).build(DSL.emptyPartType());
        public static final BlockEntityType<TileDeque> DEQUE_TYPE = FabricBlockEntityTypeBuilder.create(TileDeque::new, DEQUE).build(DSL.emptyPartType());
        public static final ScreenHandlerType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE = ScreenHandlerRegistry.registerExtended(new Identifier(TileInfChest.GUI_ID), ContainerInfChest::create);
        public static final LootFunctionType CHEST_FUNCTION = new LootFunctionType(new ContentInfChest.Serializer());
    }

    /*
    Test command
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"250000000000444465416531514645000000000",Items:[{Slot:1b,id:"minecraft:stone",Count:64b}]}} 1
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"2.5e40"}} 1
     */
}
