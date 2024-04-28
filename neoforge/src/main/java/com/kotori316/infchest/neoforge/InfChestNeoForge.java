package com.kotori316.infchest.neoforge;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.blocks.BlockDeque;
import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.blocks.ContentInfChest;
import com.kotori316.infchest.common.guis.ContainerInfChest;
import com.kotori316.infchest.common.guis.GuiInfChest;
import com.kotori316.infchest.common.tiles.TileDeque;
import com.kotori316.infchest.common.tiles.TileInfChest;
import com.kotori316.infchest.neoforge.blocks.BlockDequeNeoForge;
import com.kotori316.infchest.neoforge.blocks.BlockInfChestNeoForge;
import com.kotori316.infchest.neoforge.integration.AE2InfChestIntegration;
import com.kotori316.infchest.neoforge.packets.PacketHandler;
import com.kotori316.infchest.neoforge.tiles.TileDequeNeoForge;
import com.kotori316.infchest.neoforge.tiles.TileInfChestNeoForge;
import com.mojang.datafixers.DSL;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.kotori316.infchest.common.InfChest.modID;

@Mod(modID)
public final class InfChestNeoForge {
    public InfChestNeoForge(IEventBus modBus) {
        modBus.addListener(this::preInit);
        modBus.addListener(this::clientInit);
        AE2InfChestIntegration.onAPIAvailable(modBus);
    }

    public void preInit(FMLCommonSetupEvent event) {
    }

    public void clientInit(FMLClientSetupEvent event) {
    }

    public static class Register implements InfChest.TypeAccessor {
        public static final BlockInfChestNeoForge CHEST = new BlockInfChestNeoForge();
        public static final BlockEntityType<TileInfChestNeoForge> INF_CHEST_TYPE = BlockEntityType.Builder.of(TileInfChestNeoForge::new, CHEST).build(DSL.emptyPartType());
        public static final BlockDequeNeoForge DEQUE = new BlockDequeNeoForge();
        public static final BlockEntityType<TileDequeNeoForge> DEQUE_TYPE = BlockEntityType.Builder.of(TileDequeNeoForge::new, DEQUE).build(DSL.emptyPartType());
        public static final MenuType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE = IMenuTypeExtension.create(ContainerInfChest::create);
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

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = modID)
    public static class EventHandlers {

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(Registries.BLOCK, EventHandlers::registerBlock);
            event.register(Registries.ITEM, EventHandlers::registerItem);
            event.register(Registries.BLOCK_ENTITY_TYPE, EventHandlers::registerTile);
            event.register(Registries.MENU, EventHandlers::registerContainer);
        }

        public static void registerBlock(RegisterEvent.RegisterHelper<Block> event) {
            event.register(new ResourceLocation(modID, BlockInfChest.name), Register.CHEST);
            event.register(new ResourceLocation(modID, BlockDeque.name), Register.DEQUE);
        }

        public static void registerItem(RegisterEvent.RegisterHelper<Item> event) {
            event.register(new ResourceLocation(modID, BlockInfChest.name), Register.CHEST.itemBlock);
            event.register(new ResourceLocation(modID, BlockDeque.name), Register.DEQUE.itemBlock);
        }

        public static void registerTile(RegisterEvent.RegisterHelper<BlockEntityType<?>> event) {
            event.register(new ResourceLocation(modID, "tile." + BlockInfChest.name), Register.INF_CHEST_TYPE);
            event.register(new ResourceLocation(modID, "tile." + BlockDeque.name), Register.DEQUE_TYPE);
        }

        public static void registerContainer(RegisterEvent.RegisterHelper<MenuType<?>> event) {
            event.register(new ResourceLocation(TileInfChest.GUI_ID), Register.INF_CHEST_CONTAINER_TYPE);
        }

        @SubscribeEvent
        public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
                event.accept(Register.CHEST);
                event.accept(Register.DEQUE);
            }
        }

        @SubscribeEvent
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Register.INF_CHEST_TYPE, TileInfChestNeoForge::getCapability);
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Register.DEQUE_TYPE, TileDequeNeoForge::getCapability);
        }

        @SubscribeEvent
        public static void registerMenu(RegisterMenuScreensEvent event) {
            event.register(Register.INF_CHEST_CONTAINER_TYPE, GuiInfChest::new);
        }

        @SubscribeEvent
        public static void registerPacket(RegisterPayloadHandlersEvent event) {
            PacketHandler.init(event);
        }
    }

}
