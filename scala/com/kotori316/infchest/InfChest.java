package com.kotori316.infchest;


import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.infchest.blocks.BlockDeque;
import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.guis.GuiHandler;
import com.kotori316.infchest.packets.PacketHandler;
import com.kotori316.infchest.tiles.TileDeque;
import com.kotori316.infchest.tiles.TileInfChest;

@Mod(InfChest.modID)
public class InfChest {
    public static final String MOD_NAME = "InfChest";
    public static final String modID = "infchest";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final BlockInfChest CHEST = new BlockInfChest();
    public static final BlockDeque DEQUE = new BlockDeque();
    public static final TileEntityType<TileInfChest> INF_CHEST_TYPE = TileEntityType.Builder.create(TileInfChest::new).build(null);
    public static final TileEntityType<TileDeque> DEQUE_TYPE = TileEntityType.Builder.create(TileDeque::new).build(null);
    public static final Predicate<TileInfChest> CHEST_NOT_EMPTY = ((Predicate<TileInfChest>) TileInfChest::isEmpty).negate();
    public static final Predicate<ItemStack> STACK_NON_EMPTY = ((Predicate<ItemStack>) ItemStack::isEmpty).negate();
    public static final Predicate<String> STRING_NON_EMPTY = ((Predicate<String>) String::isEmpty).negate();

    public InfChest() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        MinecraftForge.EVENT_BUS.addListener(this::registerBlocks);
        MinecraftForge.EVENT_BUS.addListener(this::registerItem);
        MinecraftForge.EVENT_BUS.addListener(this::registerTiles);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> GuiHandler::getClientGuiElement);
    }

    public void preInit(FMLCommonSetupEvent event) {
//        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        PacketHandler.init();
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(CHEST, DEQUE);
    }

    public void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(CHEST.itemBlock, DEQUE.itemBlock);
    }

    public void registerTiles(RegistryEvent.Register<TileEntityType<?>> event) {
        event.getRegistry().register(INF_CHEST_TYPE.setRegistryName(new ResourceLocation(modID, "tile." + BlockInfChest.name)));
        event.getRegistry().register(DEQUE_TYPE.setRegistryName(new ResourceLocation(modID, "tile." + BlockDeque.name)));
    }

    /*
    Test command
    /give @p infchest:infchest 1 0 {BlockEntityTag:{item:{id:"minecraft:stone",Count:1b,Damage:0s},count:"250000000000444465416531514645000000000",Items:[{Slot:1b,id:"minecraft:stone",Count:64b,Damage:0s}]}}
     */
}
