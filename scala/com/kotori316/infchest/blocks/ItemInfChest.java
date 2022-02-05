package com.kotori316.infchest.blocks;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

final class ItemInfChest extends BlockItem {

    ItemInfChest(Block block) {
        super(block, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
        setRegistryName(InfChest.modID, BlockInfChest.name);
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
                .filter(InfChest.STACK_NON_EMPTY);
            stack.map(ItemStack::getItem)
                .map(Item::getRegistryName)
                .map(ResourceLocation::toString)
                .map(TextComponent::new)
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
        return new TextComponent(s + " items");
    }
}
