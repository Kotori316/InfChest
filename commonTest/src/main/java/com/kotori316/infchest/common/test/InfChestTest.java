package com.kotori316.infchest.common.test;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class InfChestTest {

    public record TestFunctionRecord(String batchName, String name, String structure, int tick, int setUp,
                                     boolean required, Consumer<GameTestHelper> function) {
    }

    public static Stream<TestFunctionRecord> functions(String batchName, String structure) {
        return Stream.of(
            new TestFunctionRecord(batchName, "extractWithHopperInfChest", structure, 100, 0, true, InfChestTest::extractWithHopper),
            new TestFunctionRecord(batchName, "insertWithHopperInfChest", structure, 100, 0, true, InfChestTest::insertWithHopper),
            new TestFunctionRecord(batchName, "extractWithMinecartHopperInfChest", structure, 100, 0, true, InfChestTest::extractWithMinecartHopper),
            new TestFunctionRecord(batchName, "placeInfChest", structure, 100, 0, true, InfChestTest::placeInfChest)
        );
    }

    private static final BlockPos pos = new BlockPos(0, 3, 0);

    static void placeInfChest(GameTestHelper helper) {
        helper.setBlock(pos, InfChest.accessor.CHEST());

        helper.getBlockEntity(pos, TileInfChest.class);
        helper.succeed();
    }

    static void insertWithHopper(GameTestHelper helper) {
        helper.setBlock(pos, InfChest.accessor.CHEST());
        var chest = helper.getBlockEntity(pos, TileInfChest.class);
        chest.addStack(new ItemStack(Items.STONE), BigInteger.TEN);
        chest.setChanged();

        var hopperPos = pos.above();
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState());
        var hopper = helper.getBlockEntity(hopperPos, HopperBlockEntity.class);
        hopper.setItem(0, new ItemStack(Items.STONE, 64));
        helper.runAfterDelay(48, () -> {
            if (hopper.getItem(0).getCount() != 58) {
                throw new GameTestAssertException(Component.literal("Hopper did not insert items into chest. Actual: %d".formatted(hopper.getItem(0).getCount())), (int) helper.getTick());
            }
            if (chest.getItem(1).getCount() != 16) {
                throw new GameTestAssertException(Component.literal("Chest did not insert items into hopper. Actual: %d".formatted(chest.getItem(1).getCount())), (int) helper.getTick());
            }
            helper.succeed();
        });
    }

    static void extractWithHopper(GameTestHelper helper) {
        helper.setBlock(pos, InfChest.accessor.CHEST());
        var chest = helper.getBlockEntity(pos, TileInfChest.class);
        chest.addStack(new ItemStack(Items.STONE), BigInteger.TEN);
        chest.setChanged();

        var hopperPos = pos.below();
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState());
        var hopper = helper.getBlockEntity(hopperPos, HopperBlockEntity.class);
        helper.runAfterDelay(48, () -> {
            var chestStack = chest.getItem(1);
            if (chestStack.getCount() != 4) {
                throw new GameTestAssertException(Component.literal("Hopper did not extract items from chest. Actual: %d".formatted(chestStack.getCount())), (int) helper.getTick());
            }
            var hopperStack = hopper.getItem(0);
            if (hopperStack.getCount() != 6) {
                throw new GameTestAssertException(Component.literal("Hopper did not get items from chest. Actual: %d".formatted(hopperStack.getCount())), (int) helper.getTick());
            }
            if (!chestStack.is(hopperStack.getItem())) {
                throw new GameTestAssertException(Component.literal("Items are different, chest: %s, hopper: %s".formatted(chestStack.getItem(), hopperStack.getItem())), (int) helper.getTick());
            }
            helper.succeed();
        });
    }

    static void extractWithMinecartHopper(GameTestHelper helper) {
        helper.setBlock(pos, InfChest.accessor.CHEST());
        var chest = helper.getBlockEntity(pos, TileInfChest.class);
        chest.addStack(new ItemStack(Items.STONE), BigInteger.valueOf(20000));
        chest.setChanged();
        if (!chest.totalCount().equals(BigInteger.valueOf(20000))) {
            throw new GameTestAssertException(Component.literal("Chest total count is not 20000, but %s".formatted(chest.totalCount())), (int) helper.getTick());
        }

        helper.setBlock(pos.below(2), Blocks.STONE);
        helper.setBlock(pos.below(1), Blocks.RAIL);
        helper.spawn(EntityType.HOPPER_MINECART, pos.below(1));
        var hopper = helper.findOneEntity(EntityType.HOPPER_MINECART);
        for (int i = 0; i < 4; i++) {
            hopper.setItem(i, new ItemStack(Items.STONE, 64));
        }
        hopper.setItem(4, new ItemStack(Items.STONE, 32));

        var tick = 48;
        helper.runAfterDelay(tick, () -> {
            var hopperStack = hopper.getItem(4);
            if (hopperStack.getCount() != 64) {
                throw new GameTestAssertException(Component.literal("Hopper did not get items from chest. Actual: %d".formatted(hopperStack.getCount())), (int) helper.getTick());
            }
            if (!chest.totalCount().equals(BigInteger.valueOf(20000 - 32))) {
                throw new GameTestAssertException(Component.literal("Chest items are too drained, actual: %s".formatted(chest.totalCount())), (int) helper.getTick());
            }
            helper.succeed();
        });
    }
}
