package com.kotori316.infchest.common.test;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileDeque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import java.util.stream.Stream;

public final class DequeTest {

    public static Stream<TestFunction> functions(String batchName, String structure) {
        return Stream.of(
            new TestFunction(batchName, "placeDeque", structure, 100, 0, true, DequeTest::placeDeque),
            new TestFunction(batchName, "insertFromHopper", structure, 100, 0, true, DequeTest::insertFromHopper),
            new TestFunction(batchName, "extractToHopper", structure, 100, 0, true, DequeTest::extractToHopper)
        );
    }

    static void placeDeque(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, InfChest.accessor.DEQUE());

        var tile = helper.getBlockEntity(pos);
        if (!(tile instanceof TileDeque)) {
            throw new GameTestAssertException("Expected TileDeque, but got %s".formatted(tile));
        }
        helper.succeed();
    }

    static void insertFromHopper(GameTestHelper helper) {
        // Setup Deque
        var dequePos = new BlockPos(0, 2, 0);
        helper.setBlock(dequePos, InfChest.accessor.DEQUE());
        var deque = (TileDeque) helper.getBlockEntity(dequePos);

        // Setup Hopper with stone
        var hopperPos = new BlockPos(0, 3, 0);
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState()
            .setValue(HopperBlock.FACING, Direction.DOWN));
        var hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
        var stone = new ItemStack(Items.STONE, 5);
        hopper.setItem(0, stone);

        // Wait for transfer
        helper.runAfterDelay(40, () -> {
            if (hopper.getItem(0).isEmpty() && !deque.isEmpty()) {
                helper.succeed();
            } else {
                throw new GameTestAssertException(
                    "Transfer failed. Hopper: %s, Deque: %s"
                        .formatted(hopper.getItem(0), deque.itemsList()));
            }
        });
    }

    static void extractToHopper(GameTestHelper helper) {
        // Setup Deque with items
        var dequePos = new BlockPos(0, 2, 0);
        helper.setBlock(dequePos, InfChest.accessor.DEQUE());
        var deque = (TileDeque) helper.getBlockEntity(dequePos);
        var stone = new ItemStack(Items.STONE, 5);
        deque.setItem(0, stone);

        // Setup empty Hopper below
        var hopperPos = new BlockPos(0, 1, 0);
        helper.setBlock(hopperPos, Blocks.HOPPER.defaultBlockState());
        var hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);

        // Wait for transfer
        helper.runAfterDelay(40, () -> {
            if (!hopper.getItem(0).isEmpty() && deque.isEmpty()) {
                helper.succeed();
            } else {
                throw new GameTestAssertException(
                    "Transfer failed. Hopper: %s, Deque: %s"
                        .formatted(hopper.getItem(0), deque.itemsList()));
            }
        });
    }
}
