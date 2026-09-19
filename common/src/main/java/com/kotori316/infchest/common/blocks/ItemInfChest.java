package com.kotori316.infchest.common.blocks;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

final class ItemInfChest extends BlockItem {

    ItemInfChest(BlockInfChest block) {
        super(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(InfChest.modID, BlockInfChest.name))).useBlockDescriptionPrefix());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (Optional.ofNullable(context.getPlayer()).map(Player::isCreative).orElse(Boolean.FALSE)) {
            int size = context.getItemInHand().getCount();
            InteractionResult result = super.useOn(context);
            context.getItemInHand().setCount(size);
            return result;
        } else {
            return super.useOn(context);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack chestStack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag flagIn) {
        super.appendHoverText(chestStack, context, tooltipDisplay, consumer, flagIn);
        CompoundTag n = Optional.ofNullable(chestStack.get(DataComponents.BLOCK_ENTITY_DATA)).map(TypedEntityData::copyTagWithoutId).orElse(null);
        var registry = context.registries();
        if (n != null && registry != null) {
            Optional<ItemStack> stack = ItemStack.OPTIONAL_CODEC.parse(registry.createSerializationContext(NbtOps.INSTANCE), n.getCompoundOrEmpty(TileInfChest.NBT_ITEM))
                .result()
                .filter(Predicate.not(ItemStack::isEmpty));
            stack.map(ItemStack::getItem)
                .map(BuiltInRegistries.ITEM::getKey)
                .map(Identifier::toString)
                .map(Component::literal)
                .ifPresent(consumer);
            stack.map(ItemStack::getDisplayName)
                .ifPresent(consumer);
            n.getString(TileInfChest.NBT_COUNT)
                .filter(Predicate.not(String::isEmpty))
                .map(ItemInfChest::addPostfix)
                .ifPresent(consumer);
        }
    }

    private static Component addPostfix(String s) {
        return Component.literal(s + " items");
    }
}
