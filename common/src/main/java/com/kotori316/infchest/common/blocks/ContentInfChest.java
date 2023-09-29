package com.kotori316.infchest.common.blocks;

import com.kotori316.infchest.common.InfChest;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class ContentInfChest extends LootItemConditionalFunction {
    public static final ResourceLocation LOCATION = new ResourceLocation(InfChest.modID, "content_infchest");
    public static final Codec<ContentInfChest> CODEC = RecordCodecBuilder.create(instance ->
        commonFields(instance).apply(instance, ContentInfChest::new)
    );

    protected ContentInfChest(List<LootItemCondition> conditionsIn) {
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

}
