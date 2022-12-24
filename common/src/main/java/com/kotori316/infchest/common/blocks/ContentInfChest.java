package com.kotori316.infchest.common.blocks;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import com.kotori316.infchest.common.InfChest;

public class ContentInfChest extends LootItemConditionalFunction {
    public static final ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "content_infchest");

    protected ContentInfChest(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var entity = context.getParam(LootContextParams.BLOCK_ENTITY);
        BlockInfChest.saveChestNbtToStack(entity, stack);
        BlockInfChest.saveCustomName(entity, stack);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return InfChest.accessor.CHEST_FUNCTION();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<ContentInfChest> {

        public Serializer() {
        }

        @Override
        public ContentInfChest deserialize(JsonObject object, JsonDeserializationContext deserializationContext, LootItemCondition[] conditionsIn) {
            return new ContentInfChest(conditionsIn);
        }
    }
}
