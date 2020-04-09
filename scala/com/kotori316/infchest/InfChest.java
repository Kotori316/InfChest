package com.kotori316.infchest;

import java.util.function.Predicate;

import com.mojang.datafixers.DSL;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.extensions.IForgeContainerType;
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
import com.kotori316.infchest.integration.TOP;
import com.kotori316.infchest.packets.PacketHandler;
import com.kotori316.infchest.tiles.TileDeque;
import com.kotori316.infchest.tiles.TileInfChest;

@Mod(InfChest.modID)
public class InfChest {
    public static final String MOD_NAME = "InfChest";
    public static final String modID = "infchest";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final Predicate<TileInfChest> CHEST_NOT_EMPTY = ((Predicate<TileInfChest>) TileInfChest::isEmpty).negate();
    public static final Predicate<ItemStack> STACK_NON_EMPTY = ((Predicate<ItemStack>) ItemStack::isEmpty).negate();
    public static final Predicate<String> STRING_NON_EMPTY = ((Predicate<String>) String::isEmpty).negate();

    public InfChest() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientInit);
    }

    public void preInit(FMLCommonSetupEvent event) {
//        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        PacketHandler.init();
        LootFunctionManager.registerFunction(new ContentInfChest.Serializer());
        TOP.register();
    }

    public void clientInit(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Register.INF_CHEST_CONTAINER_TYPE, GuiInfChest::new);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Register {
        public static final BlockInfChest CHEST = new BlockInfChest();
        public static final TileEntityType<TileInfChest> INF_CHEST_TYPE = TileEntityType.Builder.create(TileInfChest::new, CHEST).build(DSL.nilType());
        public static final BlockDeque DEQUE = new BlockDeque();
        public static final TileEntityType<TileDeque> DEQUE_TYPE = TileEntityType.Builder.create(TileDeque::new, DEQUE).build(DSL.nilType());
        public static final ContainerType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE = IForgeContainerType.create(ContainerInfChest::create);

        @SubscribeEvent
        public static void registerBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(CHEST, DEQUE);
        }

        @SubscribeEvent
        public static void registerItem(RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(CHEST.itemBlock, DEQUE.itemBlock);
        }

        @SubscribeEvent
        public static void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
            event.getRegistry().register(INF_CHEST_TYPE.setRegistryName(new ResourceLocation(modID, "tile." + BlockInfChest.name)));
            event.getRegistry().register(DEQUE_TYPE.setRegistryName(new ResourceLocation(modID, "tile." + BlockDeque.name)));
        }

        @SubscribeEvent
        public static void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
            event.getRegistry().register(INF_CHEST_CONTAINER_TYPE.setRegistryName(new ResourceLocation(TileInfChest.GUI_ID)));
        }
    }

    /*
    Test command
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"250000000000444465416531514645000000000",Items:[{Slot:1b,id:"minecraft:stone",Count:64b}]}} 1
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"2.5e40"}} 1
     */
}
