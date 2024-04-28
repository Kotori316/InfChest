package com.kotori316.infchest.fabric;

import com.kotori316.infchest.common.ItemDamage;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.util.Objects;

public class ItemDamageTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void ignoreCount(GameTestHelper helper) {
        var a = new ItemStack(Items.APPLE, 1);
        var b = new ItemStack(Items.APPLE, 3);
        var itemA = new ItemDamage(a);
        var itemB = new ItemDamage(b);
        if (Objects.equals(itemA, itemB)) {
            helper.succeed();
        } else {
            throw new GameTestAssertException("%s and %s must be equal.".formatted(itemA, itemB));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void respectNbt2(GameTestHelper helper) {
        var a = new ItemStack(Items.APPLE, 1);
        {
            var tag = new CompoundTag();
            tag.putInt("a", 4);
            CustomData.set(DataComponents.CUSTOM_DATA, a, tag);
        }
        var b = new ItemStack(Items.APPLE, 1);
        var itemA = new ItemDamage(a);
        var itemB = new ItemDamage(b);
        if (!Objects.equals(itemA, itemB)) {
            helper.succeed();
        } else {
            throw new GameTestAssertException("%s and %s must not be equal.".formatted(itemA, itemB));
        }
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void isSame(GameTestHelper helper) {
        var a = new ItemStack(Items.APPLE, 1);
        var b = new ItemStack(Items.APPLE, 1);
        if (!ItemStack.matches(a, b)) {
            throw new GameTestAssertException("%s and %s must be equal.".formatted(a, b));
        }
        var c = new ItemStack(Items.APPLE, 2);
        if (ItemStack.matches(a, c)) {
            throw new GameTestAssertException("%s and %s must not be equal.".formatted(a, c));
        }
        helper.succeed();
    }
}
