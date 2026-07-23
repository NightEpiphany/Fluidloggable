package com.moigferdsrte.fluidloggable.placement;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public final class DoubleHeightFluidPlacementSupport {
	private static final ThreadLocal<FluidState> UPPER_FLUID = new ThreadLocal<>();

	private DoubleHeightFluidPlacementSupport() {
	}

	public static void captureUpperFluid(final BlockPlaceContext context) {
		UPPER_FLUID.set(context.getLevel().getFluidState(context.getClickedPos().above()));
	}

	public static void discardUpperFluid() {
		UPPER_FLUID.remove();
	}

	public static void restoreUpperFluid(final BlockPlaceContext context) {
		final FluidState upperFluid = UPPER_FLUID.get();
		UPPER_FLUID.remove();
		if (upperFluid == null || upperFluid.isEmpty()) {
			return;
		}

		final Level level = context.getLevel();
		final BlockPos upperPos = context.getClickedPos().above();
		if (FluidloggedBlockStateSupport.canStoreFluid(level.getBlockState(upperPos), upperFluid.getType())) {
			((LevelExtension) level).fluidloggable$setFluid(
					upperPos,
					upperFluid,
					Block.UPDATE_ALL | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK
			);
		}
	}
}
