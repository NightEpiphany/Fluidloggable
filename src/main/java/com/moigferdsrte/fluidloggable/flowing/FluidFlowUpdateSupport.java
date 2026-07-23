package com.moigferdsrte.fluidloggable.flowing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidFlowUpdateSupport {
	private FluidFlowUpdateSupport() {
	}

	public static void scheduleAdjacentFluidTicks(
			final ServerLevel level,
			final BlockPos changedPos
	) {
		scheduleFluidTick(level, changedPos);
		for (Direction direction : Direction.values()) {
			scheduleFluidTick(level, changedPos.relative(direction));
		}
	}

	private static void scheduleFluidTick(final ServerLevel level, final BlockPos pos) {
		final FluidState fluidState = level.getFluidState(pos);
		if (!fluidState.isEmpty() && isSupportedFluid(fluidState)) {
			level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
		}
	}

	private static boolean isSupportedFluid(final FluidState state) {
		return state.getType().isSame(Fluids.WATER)
				|| state.getType().isSame(Fluids.LAVA);
	}
}
