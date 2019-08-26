package com.kotori316.infchest.blocks;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootFunction;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.world.storage.loot.conditions.ILootCondition;

import com.kotori316.infchest.InfChest;

public class ContentInfChest extends LootFunction {
    public static final ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "content_infchest");

    protected ContentInfChest(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ItemStack doApply(ItemStack stack, LootContext context) {
        TileEntity entity = context.get(LootParameters.BLOCK_ENTITY);
        BlockInfChest.saveChestNbtToStack(entity, stack);
        BlockInfChest.saveCustomName(entity, stack);
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<ContentInfChest> {

        public Serializer() {
            super(ContentInfChest.LOCATION, ContentInfChest.class);
        }

        @Override
        public ContentInfChest deserialize(JsonObject object, JsonDeserializationContext deserializationContext, ILootCondition[] conditionsIn) {
            return new ContentInfChest(conditionsIn);
        }
    }
}
