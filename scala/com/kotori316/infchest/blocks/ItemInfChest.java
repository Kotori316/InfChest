package com.kotori316.infchest.blocks;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import com.kotori316.infchest.InfChest;
import com.kotori316.infchest.tiles.TileInfChest;

class ItemInfChest extends BlockItem {

    ItemInfChest(Block block) {
        super(block, new Item.Properties().group(ItemGroup.DECORATIONS));
        setRegistryName(InfChest.modID, BlockInfChest.name);
    }

    @Override
    public ActionResultType tryPlace(BlockItemUseContext context) {
        if (Optional.ofNullable(context.getPlayer()).map(PlayerEntity::isCreative).orElse(Boolean.FALSE)) {
            int size = context.getItem().getCount();
            ActionResultType result = super.tryPlace(context);
            context.getItem().setCount(size);
            return result;
        } else {
            return super.tryPlace(context);
        }
    }

    @Override
    protected boolean onBlockPlaced(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState p_195943_5_) {
        if (world.getServer() != null) {
            CompoundNBT tag = stack.getChildTag(TileInfChest.NBT_BLOCK_TAG);
            TileEntity tileentity = world.getTileEntity(pos);
            if (tag != null && tileentity != null) {
                if (world.isRemote || !tileentity.onlyOpsCanSetNbt() || (player != null && player.canUseCommandBlock())) {
                    CompoundNBT tileNbt = tileentity.write(new CompoundNBT());
                    tileNbt.merge(tag);
                    tileNbt.putInt("x", pos.getX());
                    tileNbt.putInt("y", pos.getY());
                    tileNbt.putInt("z", pos.getZ());

                    tileentity.read(tileNbt);
                    tileentity.markDirty();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack chestStack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(chestStack, worldIn, tooltip, flagIn);
        CompoundNBT n = chestStack.getChildTag(TileInfChest.NBT_BLOCK_TAG);
        if (n != null) {
            Optional<ItemStack> stack = Optional.of(ItemStack.read(n.getCompound(TileInfChest.NBT_ITEM)))
                .filter(InfChest.STACK_NON_EMPTY);
            stack.map(ItemStack::getItem)
                .map(Item::getRegistryName)
                .map(ResourceLocation::toString)
                .map(StringTextComponent::new)
                .ifPresent(tooltip::add);
            stack.map(ItemStack::getDisplayName)
                .ifPresent(tooltip::add);
            Optional.of(n.getString(TileInfChest.NBT_COUNT))
                .filter(InfChest.STRING_NON_EMPTY)
                .map(ItemInfChest::addPostfix)
                .ifPresent(tooltip::add);
        }
    }

    private static ITextComponent addPostfix(String s) {
        return new StringTextComponent(s + " items");
    }
}
