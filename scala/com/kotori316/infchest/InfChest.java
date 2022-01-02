package com.kotori316.infchest;

import java.util.function.Predicate;

import com.mojang.datafixers.DSL;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        public static final BlockInfChest CHEST = new BlockInfChest();
        public static final BlockEntityType<TileInfChest> INF_CHEST_TYPE = BlockEntityType.Builder.of(TileInfChest::new, CHEST).build(DSL.emptyPartType());
        public static final BlockDeque DEQUE = new BlockDeque();
        public static final BlockEntityType<TileDeque> DEQUE_TYPE = BlockEntityType.Builder.of(TileDeque::new, DEQUE).build(DSL.emptyPartType());
        public static final MenuType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE = IForgeMenuType.create(ContainerInfChest::create);
        public static final LootItemFunctionType CHEST_FUNCTION = Registry.register(Registry.LOOT_FUNCTION_TYPE, ContentInfChest.LOCATION,
            new LootItemFunctionType(new ContentInfChest.Serializer()));

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(CHEST, DEQUE);
        }

        @SubscribeEvent
        public static void registerItem(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(CHEST.itemBlock, DEQUE.itemBlock);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<BlockEntityType<?>> event) {
            event.getRegistry().register(INF_CHEST_TYPE.setRegistryName(new ResourceLocation(modID, "tile." + BlockInfChest.name)));
            event.getRegistry().register(DEQUE_TYPE.setRegistryName(new ResourceLocation(modID, "tile." + BlockDeque.name)));
        }

        @SubscribeEvent
        public static void registerContainer(RegistryEvent.Register<MenuType<?>> event) {
            event.getRegistry().register(INF_CHEST_CONTAINER_TYPE.setRegistryName(new ResourceLocation(TileInfChest.GUI_ID)));
        }
    }

    /*
    Test command
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"250000000000444465416531514645000000000",Items:[{Slot:1b,id:"minecraft:stone",Count:64b}]}} 1
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"2.5e40"}} 1
     */
}
