package com.moigferdsrte.fluidloggable.mixin.placement;

import com.moigferdsrte.fluidloggable.placement.DoubleHeightFluidPlacementSupport;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoubleHighBlockItem.class)
public abstract class DoubleHighBlockItemMixin {
	@Inject(method = "placeBlock", at = @At("HEAD"))
	private void fluidloggable$captureUpperFluid(
			final BlockPlaceContext context,
			final BlockState placementState,
			final CallbackInfoReturnable<Boolean> cir
	) {
		DoubleHeightFluidPlacementSupport.captureUpperFluid(context);
	}

	@Inject(method = "placeBlock", at = @At("RETURN"))
	private void fluidloggable$discardUpperFluidAfterFailedPlacement(
			final BlockPlaceContext context,
			final BlockState placementState,
			final CallbackInfoReturnable<Boolean> cir
	) {
		if (!cir.getReturnValue()) {
			DoubleHeightFluidPlacementSupport.discardUpperFluid();
		}
	}
}
