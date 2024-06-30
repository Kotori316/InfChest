package com.kotori316.infchest.forge;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.blocks.BlockDeque;
import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.blocks.ContentInfChest;
import com.kotori316.infchest.common.guis.ContainerInfChest;
import com.kotori316.infchest.common.guis.GuiInfChest;
import com.kotori316.infchest.common.tiles.TileDeque;
import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.forge.blocks.BlockDequeForge;
import com.kotori316.infchest.forge.blocks.BlockInfChestForge;
import com.kotori316.infchest.forge.integration.AE2InfChestIntegration;
import com.kotori316.infchest.forge.integration.RsInfChestIntegration;
import com.kotori316.infchest.forge.packets.PacketHandler;
import com.kotori316.infchest.forge.tiles.TileDequeForge;
import com.kotori316.infchest.forge.tiles.TileInfChestForge;
import com.mojang.datafixers.DSL;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import static com.kotori316.infchest.common.InfChest.modID;

@Mod(modID)
public final class InfChestForge {
    public InfChestForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
    }

    public void preInit(FMLCommonSetupEvent event) {
        PacketHandler.init();
        AE2InfChestIntegration.onAPIAvailable();
        RsInfChestIntegration.onAPIAvailable();
    }

    public void clientInit(FMLClientSetupEvent event) {
        MenuScreens.register(Register.INF_CHEST_CONTAINER_TYPE, GuiInfChest::new);
    }

    public static class Register implements InfChest.TypeAccessor {
        public static final BlockInfChestForge CHEST = new BlockInfChestForge();
        public static final BlockEntityType<TileInfChestForge> INF_CHEST_TYPE = BlockEntityType.Builder.of(TileInfChestForge::new, CHEST).build(DSL.emptyPartType());
        public static final BlockDequeForge DEQUE = new BlockDequeForge();
        public static final BlockEntityType<TileDequeForge> DEQUE_TYPE = BlockEntityType.Builder.of(TileDequeForge::new, DEQUE).build(DSL.emptyPartType());
        public static final MenuType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE = IForgeMenuType.create(ContainerInfChest::create);
        public static final LootItemFunctionType<ContentInfChest> CHEST_FUNCTION = Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, ContentInfChest.LOCATION,
            new LootItemFunctionType<>(ContentInfChest.CODEC));

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
        public LootItemFunctionType<ContentInfChest> CHEST_FUNCTION() {
            return CHEST_FUNCTION;
        }

        @Override
        public MenuType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE() {
            return INF_CHEST_CONTAINER_TYPE;
        }

        @Override
        public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
        }

        static {
            InfChest.accessor = new Register();
        }
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
            event.register(ResourceLocation.fromNamespaceAndPath(modID, BlockInfChest.name), Register.CHEST);
            event.register(ResourceLocation.fromNamespaceAndPath(modID, BlockDeque.name), Register.DEQUE);
        }

        public static void registerItem(RegisterEvent.RegisterHelper<Item> event) {
            event.register(ResourceLocation.fromNamespaceAndPath(modID, BlockInfChest.name), Register.CHEST.itemBlock);
            event.register(ResourceLocation.fromNamespaceAndPath(modID, BlockDeque.name), Register.DEQUE.itemBlock);
        }

        public static void registerTile(RegisterEvent.RegisterHelper<BlockEntityType<?>> event) {
            event.register(ResourceLocation.fromNamespaceAndPath(modID, "tile." + BlockInfChest.name), Register.INF_CHEST_TYPE);
            event.register(ResourceLocation.fromNamespaceAndPath(modID, "tile." + BlockDeque.name), Register.DEQUE_TYPE);
        }

        public static void registerContainer(RegisterEvent.RegisterHelper<MenuType<?>> event) {
            event.register(ResourceLocation.parse(TileInfChest.GUI_ID), Register.INF_CHEST_CONTAINER_TYPE);
        }

        @SubscribeEvent
        public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
                event.accept(Register.CHEST);
                event.accept(Register.DEQUE);
            }
        }
    }

}
