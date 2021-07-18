package com.kotori316.infchest.blocks;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import com.kotori316.infchest.tiles.TileInfChest;

final class ItemInfChest extends BlockItem {

    ItemInfChest(Block block) {
        super(block, new Item.Settings().group(ItemGroup.DECORATIONS));
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        if (Optional.ofNullable(context.getPlayer()).map(PlayerEntity::isCreative).orElse(Boolean.FALSE)) {
            var size = context.getStack().getCount();
            var result = super.place(context);
            context.getStack().setCount(size);
            return result;
        } else {
            return super.place(context);
        }
    }

    @Override
    protected boolean place(ItemPlacementContext context, BlockState state) {
        return super.place(context, state);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if (world.getServer() != null) {
            var tag = stack.getSubNbt(TileInfChest.NBT_BLOCK_TAG);
            var tileentity = world.getBlockEntity(pos);
            if (tag != null && tileentity != null) {
                if (world.isClient || !tileentity.copyItemDataRequiresOperator() || (player != null && player.isCreativeLevelTwoOp())) {
                    var tileNbt = tileentity.writeNbt(new NbtCompound());
                    tileNbt.copyFrom(tag);
                    tileNbt.putInt("x", pos.getX());
                    tileNbt.putInt("y", pos.getY());
                    tileNbt.putInt("z", pos.getZ());

                    tileentity.readNbt(tileNbt);
                    tileentity.markDirty();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack chestStack, @Nullable World worldIn, List<Text> tooltip, TooltipContext flagIn) {
        super.appendTooltip(chestStack, worldIn, tooltip, flagIn);
        var n = chestStack.getSubNbt(TileInfChest.NBT_BLOCK_TAG);
        if (n != null) {
            var stack = Optional.of(ItemStack.fromNbt(n.getCompound(TileInfChest.NBT_ITEM)))
                .filter(Predicate.not(ItemStack::isEmpty));
            stack.map(ItemStack::getItem)
                .map(Registry.ITEM::getId)
                .map(Identifier::toString)
                .map(LiteralText::new)
                .ifPresent(tooltip::add);
            stack.map(ItemStack::getName)
                .ifPresent(tooltip::add);
            Optional.of(n.getString(TileInfChest.NBT_COUNT))
                .filter(Predicate.not(String::isEmpty))
                .map(s -> new BigInteger(s).add(TileInfChest.countInInventory(n)).toString())
                .map(ItemInfChest::addPostfix)
                .ifPresent(tooltip::add);
        }
    }

    private static Text addPostfix(String s) {
        return new LiteralText(s + " items");
    }
}
