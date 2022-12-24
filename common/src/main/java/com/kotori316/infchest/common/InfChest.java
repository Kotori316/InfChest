package com.kotori316.infchest.common;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kotori316.infchest.common.blocks.BlockInfChest;
import com.kotori316.infchest.common.guis.ContainerInfChest;
import com.kotori316.infchest.common.tiles.TileDeque;
import com.kotori316.infchest.common.tiles.TileInfChest;

public final class InfChest {
    public static final String MOD_NAME = "InfChest";
    public static final String modID = "infchest";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    /*
    Test command
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"250000000000444465416531514645000000000",Items:[{Slot:1b,id:"minecraft:stone",Count:64b}]}} 1
    /give @p infchest:infchest{BlockEntityTag:{item:{id:"minecraft:stone",Count:1b},count:"2.5e40"}} 1
     */

    public static TypeAccessor accessor;

    public interface TypeAccessor {
        BlockEntityType<? extends TileInfChest> INF_CHEST_TYPE();

        BlockEntityType<? extends TileDeque> DEQUE_TYPE();

        BlockInfChest CHEST();

        LootItemFunctionType CHEST_FUNCTION();
        MenuType<ContainerInfChest> INF_CHEST_CONTAINER_TYPE();

        boolean isModLoaded(String modId);
    }
}
