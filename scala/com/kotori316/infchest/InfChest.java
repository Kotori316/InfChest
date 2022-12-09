package com.kotori316.infchest;

import java.util.function.Predicate;

import com.mojang.datafixers.DSL;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.infchest.blocks.BlockDeque;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.blocks.ContentInfChest;
import com.kotori316.infchest.guis.ContainerInfChest;
import com.kotori316.infchest.guis.GuiInfChest;
import com.kotori316.infchest.integration.AE2InfChestIntegration;
import com.kotori316.infchest.packets.PacketHandler;
import com.kotori316.infchest.tiles.TileDeque;
import com.kotori316.infchest.tiles.TileInfChest;

@Mod(InfChest.modID)
public class InfChest {
    public static final String MOD_NAME = "InfChest";
    public static final String modID = "infchest";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final Predicate<ItemStack> STACK_NON_EMPTY = Predicate.not(ItemStack::isEmpty);

    public InfChest() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
    }

    public void preInit(FMLCommonSetupEvent event) {
        PacketHandler.init();
        AE2InfChestIntegration.onAPIAvailable();
    }

    public void clientInit(FMLClientSetupEvent event) {
        MenuScreens.register(Register.INF_CHEST_CONTAINER_TYPE, GuiInfChest::new);
    }

    public static class Register {
        public static final BlockInfChest CHEST = new BlockInfChest();
        public static final BlockEntityType<TileInfChest> INF_CHEST_TYPE = BlockEntityType.Builder.of(TileInfChest::new, CHEST).build(DSL.emptyPartType());
        public static final BlockDeque DEQUE = new BlockDeque();
        public static final BlockEntityType<TileDeque> DEQUE_TYPE = BlockEntityType.Builder.of(TileDeque::new, DEQUE).build(DSL.emptyPartType());
        public static final MenuType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE = IForgeMenuType.create(ContainerInfChest::create);
        public static final LootItemFunctionType CHEST_FUNCTION = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ContentInfChest.LOCATION,
            new LootItemFunctionType(new ContentInfChest.Serializer()));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandlers {

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.BLOCK, EventHandlers::registerBlock);
            event.register(Registries.ITEM, EventHandlers::registerItem);
            event.register(Registries.BLOCK_ENTITY_TYPE, EventHandlers::registerTile);
            event.register(Registries.MENU, EventHandlers::registerContainer);
        }

        public static void registerBlock(RegisterEvent.RegisterHelper<Block> event) {
            event.register(new ResourceLocation(InfChest.modID, BlockInfChest.name), Register.CHEST);
            event.register(new ResourceLocation(InfChest.modID, BlockDeque.name), Register.DEQUE);
        }

        public static void registerItem(RegisterEvent.RegisterHelper<Item> event) {
            event.register(new ResourceLocation(InfChest.modID, BlockInfChest.name), Register.CHEST.itemBlock);
            event.register(new ResourceLocation(InfChest.modID, BlockDeque.name), Register.DEQUE.itemBlock);
        }

        public static void registerTile(RegisterEvent.RegisterHelper<BlockEntityType<?>> event) {
            event.register(new ResourceLocation(modID, "tile." + BlockInfChest.name), Register.INF_CHEST_TYPE);
            event.register(new ResourceLocation(modID, "tile." + BlockDeque.name), Register.DEQUE_TYPE);
        }

        public static void registerContainer(RegisterEvent.RegisterHelper<MenuType<?>> event) {
            event.register(new ResourceLocation(TileInfChest.GUI_ID), Register.INF_CHEST_CONTAINER_TYPE);
        }

        @SubscribeEvent
        public static void creativeTab(CreativeModeTabEvent.BuildContents event) {
            event.registerSimple(CreativeModeTabs.FUNCTIONAL_BLOCKS,
                Register.CHEST, Register.DEQUE);
        }
    }

    /*
    Test command
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"250000000000444465416531514645000000000",Items:[{Slot:1b,id:"minecraft:stone",Count:64b}]}} 1
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"2.5e40"}} 1
     */
}
