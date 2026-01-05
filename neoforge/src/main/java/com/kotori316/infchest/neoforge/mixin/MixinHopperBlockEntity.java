package com.kotori316.infchest.neoforge.mixin;

import com.kotori316.infchest.common.InfChest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public final class MixinHopperBlockEntity {

    @Inject(method = "getBlockContainer", at = @At("HEAD"), cancellable = true)
    private static void overrideInfChestContainer(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Container> cir) {
        if (state.is(InfChest.accessor.CHEST())) {
            // In Neo, the container this method returned is used to extract items from hopper, but it causes some error, so I force to use item handler instead.
            cir.setReturnValue(null);
        }
    }
}
