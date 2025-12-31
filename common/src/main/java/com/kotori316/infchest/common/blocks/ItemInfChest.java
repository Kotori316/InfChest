package com.kotori316.infchest.common.blocks;

import com.kotori316.infchest.common.InfChest;
import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;

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

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        if (world.getServer() != null) {
            CompoundTag tag = Optional.ofNullable(stack.get(DataComponents.BLOCK_ENTITY_DATA)).map(TypedEntityData::copyTagWithoutId).orElse(null);
            BlockEntity entity = world.getBlockEntity(pos);
            if (tag != null && entity != null) {
                if (world.isClientSide() || !entity.getType().onlyOpCanSetNbt() || (player != null && player.canUseGameMasterBlocks())) {
                    CompoundTag tileNbt = entity.saveWithoutMetadata(world.registryAccess());
                    tileNbt.merge(tag);
                    tileNbt.putInt("x", pos.getX());
                    tileNbt.putInt("y", pos.getY());
                    tileNbt.putInt("z", pos.getZ());
                    try (var reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), InfChest.LOGGER)) {
                        entity.loadCustomOnly(TagValueInput.create(reporter, world.registryAccess(), tileNbt));
                    }
                    entity.setChanged();
                    return true;
                }
            }
        }
        return false;
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
