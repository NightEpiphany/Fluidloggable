package com.moigferdsrte.fluidloggable.mixin.flowing;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.WaterFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterFluid.class)
public abstract class WaterFluidMixin {
	@Inject(method = "canBeReplacedWith", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$letWaterFlowThroughWaterloggedBlocks(
		final FluidState state,
		final BlockGetter level,
		final BlockPos pos,
		final Fluid other,
		final Direction direction,
		final CallbackInfoReturnable<Boolean> cir
	) {
		if (WaterloggableBlockSupport.isWaterlogged(level.getBlockState(pos)) && fluidloggable$isSameWater(state.getType()) && fluidloggable$isSameWater(other)) {
			cir.setReturnValue(true);
		}
	}

	@Unique
	private static boolean fluidloggable$isSameWater(final Fluid fluid) {
		return fluid.isSame(Fluids.WATER);
	}
}
