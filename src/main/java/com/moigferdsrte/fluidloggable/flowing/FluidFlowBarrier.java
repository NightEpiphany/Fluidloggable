package com.moigferdsrte.fluidloggable.flowing;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class FluidFlowBarrier {
	private FluidFlowBarrier() {
	}

	public static boolean blocksPassage(
			final Direction direction,
			final BlockState sourceState,
			final BlockState targetState
	) {
		return blocksPassage(
				FluidloggableConfig.isTrapdoorFluidBlockingEnabled(),
				direction,
				sourceState,
				targetState
		);
	}

	static boolean blocksPassage(
			final boolean enabled,
			final Direction direction,
			final BlockState sourceState,
			final BlockState targetState
	) {
		if (!enabled) {
			return false;
		}

		return hasRaisedTrapdoorOnBoundary(sourceState, direction)
				|| hasRaisedTrapdoorOnBoundary(targetState, direction.getOpposite());
	}

	private static boolean hasRaisedTrapdoorOnBoundary(final BlockState state, final Direction boundary) {
		if (!(state.getBlock() instanceof TrapDoorBlock) || !state.getValue(TrapDoorBlock.OPEN)) {
			return false;
		}

		// 原版旋转表中的 FACING 与竖起后门板实际所在边界相反。
		return state.getValue(TrapDoorBlock.FACING).getOpposite() == boundary;
	}
}
