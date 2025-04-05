package com.kotori316.infchest.common.test;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.util.function.Consumer;
import java.util.stream.Stream;

public final class DequeTest {

    public record TestFunctionRecord(String batchName, String name, String structure, int tick, int setUp,
                                     boolean required, Consumer<GameTestHelper> function) {
    }

    public static Stream<TestFunctionRecord> functions(String batchName, String structure) {
        return Stream.of(
            new TestFunctionRecord(batchName, "placeDeque", structure, 100, 0, true, DequeTest::placeDeque),
            new TestFunctionRecord(batchName, "insertFromHopper", structure, 100, 0, true, DequeTest::insertFromHopper),
            new TestFunctionRecord(batchName, "extractToHopper", structure, 100, 0, true, DequeTest::extractToHopper)
        );
    }

    static void placeDeque(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.DEQUE());

        helper.getBlockEntity(pos, TileDeque.class);
        helper.succeed();
    }

    static void insertFromHopper(GameTestHelper helper) {
        // Setup Deque
        var dequePos = new BlockPos(0, 2, 0);
        helper.setBlock(dequePos, InfChest.accessor.DEQUE());
        var deque = helper.getBlockEntity(dequePos, TileDeque.class);

        // Setup Hopper with stone
        var hopperPos = new BlockPos(0, 3, 0);
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState()
            .setValue(HopperBlock.FACING, Direction.DOWN));
        var hopper = helper.getBlockEntity(hopperPos, HopperBlockEntity.class);
        var stone = new ItemStack(Items.STONE, 5);
        hopper.setItem(0, stone);

        // Wait for transfer
        helper.runAfterDelay(40, () -> {
            if (hopper.getItem(0).isEmpty() && !deque.isEmpty()) {
                helper.succeed();
            } else {
                throw new GameTestAssertException(Component.literal(
                    "Transfer failed. Hopper: %s, Deque: %s"
                        .formatted(hopper.getItem(0), deque.itemsList())
                ), (int) helper.getTick());
            }
        });
    }

    static void extractToHopper(GameTestHelper helper) {
        // Setup Deque with items
        var dequePos = new BlockPos(0, 2, 0);
        helper.setBlock(dequePos, InfChest.accessor.DEQUE());
        var deque = helper.getBlockEntity(dequePos, TileDeque.class);
        var stone = new ItemStack(Items.STONE, 5);
        deque.setItem(0, stone);

        // Setup empty Hopper below
        var hopperPos = new BlockPos(0, 1, 0);
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState());
        var hopper = helper.getBlockEntity(hopperPos, HopperBlockEntity.class);

        // Wait for transfer
        helper.runAfterDelay(40, () -> {
            if (!hopper.getItem(0).isEmpty() && deque.isEmpty()) {
                helper.succeed();
            } else {
                throw new GameTestAssertException(Component.literal(
                    "Transfer failed. Hopper: %s, Deque: %s"
                        .formatted(hopper.getItem(0), deque.itemsList())
                ), (int) helper.getTick());
            }
        });
    }
}
