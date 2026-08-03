package com.moigferdsrte.fluidloggable.mixin.flowing;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LavaFluid.class)
public abstract class LavaFluidMixin {
	@Inject(method = "canBeReplacedWith", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$letLavaFlowThroughLavaloggedBlocks(
			final FluidState state,
			final BlockGetter level,
			final BlockPos pos,
			final Fluid other,
			final Direction direction,
			final CallbackInfoReturnable<Boolean> cir
	) {
		final FluidState storedState = level.getFluidState(pos);
		if (FluidloggedBlockStateSupport.containsFluid(level, pos, level.getBlockState(pos), Fluids.LAVA)
				&& storedState.getType().isSame(Fluids.LAVA)
				&& !storedState.isSource()
				&& other.isSame(Fluids.LAVA)) {
			cir.setReturnValue(true);
		}
	}
}
