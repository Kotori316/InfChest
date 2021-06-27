package com.kotori316.infchest.blocks;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;

import com.kotori316.infchest.InfChest;

public class ContentInfChest extends ConditionalLootFunction {
    public static final Identifier LOCATION = new Identifier(InfChest.modID, "content_infchest");

    protected ContentInfChest(LootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ItemStack process(ItemStack stack, LootContext context) {
        var entity = context.get(LootContextParameters.BLOCK_ENTITY);
        BlockInfChest.saveChestNbtToStack(entity, stack);
        BlockInfChest.saveCustomName(entity, stack);
        return stack;
    }

    @Override
    public LootFunctionType getType() {
        return InfChest.Register.CHEST_FUNCTION;
    }

    public static class Serializer extends ConditionalLootFunction.Serializer<ContentInfChest> {

        public Serializer() {
        }

        @Override
        public ContentInfChest fromJson(JsonObject object, JsonDeserializationContext deserializationContext, LootCondition[] conditionsIn) {
            return new ContentInfChest(conditionsIn);
        }
    }
}
