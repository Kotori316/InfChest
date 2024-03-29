package com.kotori316.infchest.common.blocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.common.tiles.TileInfChest;

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
            CompoundTag tag = BlockItem.getBlockEntityData(stack);
            BlockEntity tileentity = world.getBlockEntity(pos);
            if (tag != null && tileentity != null) {
                if (world.isClientSide || !tileentity.onlyOpCanSetNbt() || (player != null && player.canUseGameMasterBlocks())) {
                    CompoundTag tileNbt = tileentity.saveWithoutMetadata();
                    tileNbt.merge(tag);
                    tileNbt.putInt("x", pos.getX());
                    tileNbt.putInt("y", pos.getY());
                    tileNbt.putInt("z", pos.getZ());

                    tileentity.load(tileNbt);
                    tileentity.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack chestStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(chestStack, worldIn, tooltip, flagIn);
        CompoundTag n = BlockItem.getBlockEntityData(chestStack);
        if (n != null) {
            Optional<ItemStack> stack = Optional.of(ItemStack.of(n.getCompound(TileInfChest.NBT_ITEM)))
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
