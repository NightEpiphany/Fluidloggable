package com.moigferdsrte.fluidloggable.compat.comforts.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.illusivesoulworks.comforts.common.block.BaseComfortsBlock")
public abstract class BaseComfortsBlockMixin {
	@Redirect(
			method = "playerWillDestroy",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
			)
	)
	private boolean fluidloggable$preserveExactFluidInOtherHalf(
			final Level level,
			final BlockPos pos,
			final BlockState blockState,
			final int updateFlags
	) {
		final FluidState storedFluid = level.getFluidState(pos);
		final BlockState replacement = storedFluid.isEmpty()
				? Blocks.AIR.defaultBlockState()
				: storedFluid.createLegacyBlock();
		return level.setBlock(pos, replacement, updateFlags);
	}
}
