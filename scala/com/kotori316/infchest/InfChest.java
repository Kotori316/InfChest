package com.kotori316.infchest;


import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.infchest.blocks.BlockInfChest;
import com.kotori316.infchest.guis.GuiHandler;
import com.kotori316.infchest.packets.PacketHandler;
import com.kotori316.infchest.tiles.TileInfChest;

@Mod(name = InfChest.MOD_NAME, modid = InfChest.modID, version = "${version}", certificateFingerprint = "@FINGERPRINT@")
public class InfChest {
    public static final String MOD_NAME = "InfChest";
    public static final String modID = "infchest";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
    public static final InfChest instance;

    public static final BlockInfChest CHEST = new BlockInfChest();
    public static final Predicate<TileInfChest> CHEST_IS_EMPTY = TileInfChest::isEmpty;
    public static final Predicate<ItemStack> STACK_NON_EMPTY = ((Predicate<ItemStack>) ItemStack::isEmpty).negate();
    public static final Predicate<String> STRING_EMPTY = String::isEmpty;

    static {
        instance = new InfChest();
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PacketHandler.init();
    }

    @Mod.InstanceFactory
    public static InfChest getInstance() {
        return instance;
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(CHEST);
        TileEntity.register(modID + ":tile." + BlockInfChest.name, TileInfChest.class);
    }

    @SubscribeEvent
    public void registerItem(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(CHEST.itemBlock);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(CHEST.itemBlock, 0, new ModelResourceLocation(
                Objects.requireNonNull(CHEST.getRegistryName()), "inventory"));
    }

    /*
    Test command
    /give @p infchest:infchest 1 0 {BlockEntityTag:{item:{id:"minecraft:stone",Count:1b,Damage:0s},count:"250000000000444465416531514645000000000",Items:[{Slot:1b,id:"minecraft:stone",Count:64b,Damage:0s}]}}
     */
}
