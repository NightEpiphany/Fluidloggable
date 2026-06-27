package com.moigferdsrte.fluidloggable.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public interface LevelExtension {
	boolean fluidloggable$setFluid(BlockPos pos, FluidState fluidState, int flags, int maxUpdateDepth);

	default boolean fluidloggable$setFluid(final BlockPos pos, final FluidState fluidState, final int flags) {
		return this.fluidloggable$setFluid(pos, fluidState, flags, 512);
	}

	boolean fluidloggable$setBlockAndInsertFluidIfPossible(BlockPos pos, BlockState state, int flags);
}
