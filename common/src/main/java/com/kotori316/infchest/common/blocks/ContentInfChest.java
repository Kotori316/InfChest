package com.kotori316.infchest.common.blocks;

import com.kotori316.infchest.common.InfChest;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class ContentInfChest extends LootItemConditionalFunction {
    public static final Identifier LOCATION = Identifier.fromNamespaceAndPath(InfChest.modID, "content_infchest");
    public static final MapCodec<ContentInfChest> CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance).apply(instance, ContentInfChest::new)
    );

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected ContentInfChest(Optional<Holder<LootItemCondition>> conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        var entity = context.getOptional(LootContextParams.BLOCK_ENTITY);
        BlockInfChest.saveChestNbtToStack(entity, stack);
        BlockInfChest.saveCustomName(entity, stack);
        return stack;
    }

    @Override
    public MapCodec<ContentInfChest> codec() {
        return CODEC;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return LootItemConditionalFunction.simpleBuilder(ContentInfChest::new);
    }
}
