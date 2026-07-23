package com.moigferdsrte.fluidloggable.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;

public final class LavaloggableBlockSupport {
	public static final BooleanProperty LAVALOGGED = BooleanProperty.create("lavalogged");

	private LavaloggableBlockSupport() {
	}

	public static boolean isLavalogged(final BlockState state) {
		return state.hasProperty(LAVALOGGED) && state.getValue(LAVALOGGED);
	}

	public static boolean canStoreLava(final BlockState state) {
		return state.hasProperty(LAVALOGGED);
	}

	public static void scheduleLavaTick(
			final LevelReader level,
			final ScheduledTickAccess ticks,
			final BlockPos pos,
			final BlockState state
	) {
		if (isLavalogged(state)) {
			ticks.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
		}
	}

	public static void scheduleLavaTick(final Level level, final BlockPos pos, final BlockState state) {
		if (!level.isClientSide() && isLavalogged(state)) {
			level.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
		}
	}
}
