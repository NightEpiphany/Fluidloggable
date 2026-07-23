package com.moigferdsrte.fluidloggable.mixin.state;

import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$lavaTakesPriority(final CallbackInfoReturnable<FluidState> cir) {
		final BlockState state = (BlockState) (Object) this;
		if (LavaloggableBlockSupport.isLavalogged(state)) {
			cir.setReturnValue(Fluids.LAVA.getSource(false));
		}
	}

	@Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$lavaEmitsLight(final CallbackInfoReturnable<Integer> cir) {
		final BlockState state = (BlockState) (Object) this;
		if (LavaloggableBlockSupport.isLavalogged(state)) {
			cir.setReturnValue(LavaFluid.LIGHT_EMISSION);
		}
	}
}
