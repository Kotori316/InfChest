package com.kotori316.infchest.common.blocks;

import com.kotori316.infchest.common.tiles.TileInfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

final class ItemInfChest extends BlockItem {

    ItemInfChest(Block block) {
        super(block, new Item.Properties());
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
            CompoundTag tag = Optional.ofNullable(stack.get(DataComponents.BLOCK_ENTITY_DATA)).map(CustomData::copyTag).orElse(null);
            BlockEntity entity = world.getBlockEntity(pos);
            if (tag != null && entity != null) {
                if (world.isClientSide || !entity.onlyOpCanSetNbt() || (player != null && player.canUseGameMasterBlocks())) {
                    CompoundTag tileNbt = entity.saveWithoutMetadata(world.registryAccess());
                    tileNbt.merge(tag);
                    tileNbt.putInt("x", pos.getX());
                    tileNbt.putInt("y", pos.getY());
                    tileNbt.putInt("z", pos.getZ());

                    entity.loadCustomOnly(tileNbt, world.registryAccess());
                    entity.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack chestStack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(chestStack, context, tooltip, flagIn);
        CompoundTag n = Optional.ofNullable(chestStack.get(DataComponents.BLOCK_ENTITY_DATA)).map(CustomData::copyTag).orElse(null);
        var registry = context.registries();
        if (n != null && registry != null) {
            Optional<ItemStack> stack = ItemStack.parse(registry, n.getCompound(TileInfChest.NBT_ITEM))
                .filter(Predicate.not(ItemStack::isEmpty));
            stack.map(ItemStack::getItem)
                .map(BuiltInRegistries.ITEM::getKey)
                .map(ResourceLocation::toString)
                .map(Component::literal)
                .ifPresent(tooltip::add);
            stack.map(ItemStack::getDisplayName)
                .ifPresent(tooltip::add);
            Optional.of(n.getString(TileInfChest.NBT_COUNT))
                .filter(Predicate.not(String::isEmpty))
                .map(ItemInfChest::addPostfix)
                .ifPresent(tooltip::add);
        }
    }

    private static Component addPostfix(String s) {
        return Component.literal(s + " items");
    }
}
