package com.kotori316.infchest.blocks;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.tiles.TileInfChest;

final class ItemInfChest extends BlockItem {

    ItemInfChest(Block block) {
        super(block, new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS));
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        if (Optional.ofNullable(context.getPlayer()).map(Player::isCreative).orElse(Boolean.FALSE)) {
            var size = context.getItemInHand().getCount();
            var result = super.place(context);
            context.getItemInHand().setCount(size);
            return result;
        } else {
            return super.place(context);
        }
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return super.placeBlock(context, state);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        if (world.getServer() != null) {
            var tag = BlockItem.getBlockEntityData(stack);
            var tileentity = world.getBlockEntity(pos);
            if (tag != null && tileentity != null) {
                if (world.isClientSide || !tileentity.onlyOpCanSetNbt() || (player != null && player.canUseGameMasterBlocks())) {
                    var tileNbt = tileentity.saveWithoutMetadata();
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
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack chestStack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(chestStack, worldIn, tooltip, flagIn);
        var n = chestStack.getTagElement(TileInfChest.NBT_BLOCK_TAG);
        if (n != null) {
            var stack = Optional.of(ItemStack.of(n.getCompound(TileInfChest.NBT_ITEM)))
                .filter(Predicate.not(ItemStack::isEmpty));
            stack.map(ItemStack::getItem)
                .map(Registry.ITEM::getKey)
                .map(ResourceLocation::toString)
                .map(Component::literal)
                .ifPresent(tooltip::add);
            stack.map(ItemStack::getHoverName)
                .ifPresent(tooltip::add);
            Optional.of(n.getString(TileInfChest.NBT_COUNT))
                .filter(Predicate.not(String::isEmpty))
                .map(s -> new BigInteger(s).add(TileInfChest.countInInventory(n)).toString())
                .map(ItemInfChest::addPostfix)
                .ifPresent(tooltip::add);
        }
    }

    private static Component addPostfix(String s) {
        return Component.literal(s + " items");
    }
}
