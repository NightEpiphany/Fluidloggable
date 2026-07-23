package com.moigferdsrte.fluidloggable.flowing;

import com.moigferdsrte.fluidloggable.block.FluidloggableBlockTags;
import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FluidFlowBarrier {
	private static final int VOXELS_PER_AXIS = 16;
	private static final int MAX_CACHE_ENTRIES = 4096;
	private static final ConcurrentMap<ShapeCacheKey, FlowingFluidBehavior> SHAPE_CACHE = new ConcurrentHashMap<>();

	private FluidFlowBarrier() {
	}

	public static boolean blocksPassage(
			final Direction direction,
			final BlockGetter level,
			final BlockPos sourcePos,
			final BlockState sourceState,
			final BlockPos targetPos,
			final BlockState targetState,
			final FluidState flowingState
	) {
		return blocksPassage(
				FluidloggableConfig.isCollisionShapeFluidBlockingEnabled(),
				direction,
				level,
				sourcePos,
				sourceState,
				targetPos,
				targetState,
				flowingState
		);
	}

	/**
	 * Checks whether fluid already stored in the source block can leave through
	 * the requested face. A source fluid can leave a CONTAIN face, while a
	 * flowing fluid remains contained by that face.
	 */
	public static boolean blocksSourceExit(
			final Direction direction,
			final BlockGetter level,
			final BlockPos sourcePos,
			final BlockState sourceState,
			final FluidState flowingState
	) {
		return blocksSourceExit(
				FluidloggableConfig.isCollisionShapeFluidBlockingEnabled(),
				direction,
				level,
				sourcePos,
				sourceState,
				flowingState
		);
	}

	static boolean blocksSourceExit(
			final boolean enabled,
			final Direction direction,
			final BlockGetter level,
			final BlockPos sourcePos,
			final BlockState sourceState,
			final FluidState flowingState
	) {
		if (!enabled) {
			return false;
		}

		// behavior() starts at the face receiving fluid; a source exits through the opposite face.
		final FlowingFluidBehavior sourceBehavior = behavior(
				direction.getOpposite(),
				level,
				sourcePos,
				sourceState,
				flowingState
		);
		return sourceBehavior == FlowingFluidBehavior.BLOCK
				|| sourceBehavior == FlowingFluidBehavior.CONTAIN && !flowingState.isSource();
	}

	/**
	 * Checks whether fluid can enter the target block from the requested face.
	 * CONTAIN intentionally allows entry so the fluid can be stored in that
	 * block instead of oscillating at its boundary.
	 */
	public static boolean blocksTargetEntry(
			final Direction direction,
			final BlockGetter level,
			final BlockPos targetPos,
			final BlockState targetState,
			final FluidState flowingState
	) {
		return blocksTargetEntry(
				FluidloggableConfig.isCollisionShapeFluidBlockingEnabled(),
				direction,
				level,
				targetPos,
				targetState,
				flowingState
		);
	}

	static boolean blocksTargetEntry(
			final boolean enabled,
			final Direction direction,
			final BlockGetter level,
			final BlockPos targetPos,
			final BlockState targetState,
			final FluidState flowingState
	) {
		return enabled
				&& behavior(direction, level, targetPos, targetState, flowingState)
						== FlowingFluidBehavior.BLOCK;
	}

	static boolean blocksPassage(
			final boolean enabled,
			final Direction direction,
			final BlockGetter level,
			final BlockPos sourcePos,
			final BlockState sourceState,
			final BlockPos targetPos,
			final BlockState targetState,
			final FluidState flowingState
	) {
		if (!enabled) {
			return false;
		}

		return blocksSourceExit(
				enabled,
				direction,
				level,
				sourcePos,
				sourceState,
				flowingState
		) || blocksTargetEntry(
				enabled,
				direction,
				level,
				targetPos,
				targetState,
				flowingState
		);
	}

	public static FlowingFluidBehavior behavior(
			final Direction direction,
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state,
			final FluidState flowingState
	) {
		if (state.is(FluidloggableBlockTags.FLUID_PASS_BLOCKS)
				|| direction.getAxis() == Direction.Axis.Y) {
			return FlowingFluidBehavior.PASS;
		}

		final int fluidHeight = fluidHeightPixels(flowingState);
		if (fluidHeight == 0) {
			return FlowingFluidBehavior.PASS;
		}

		final VoxelShape collisionShape = state.getCollisionShape(level, pos);
		if (collisionShape.isEmpty()) {
			return FlowingFluidBehavior.PASS;
		}

		if (state.getBlock().hasDynamicShape()) {
			return classify(direction, collisionShape, fluidHeight);
		}

		final ShapeCacheKey key = new ShapeCacheKey(state, direction, fluidHeight);
		final FlowingFluidBehavior cached = SHAPE_CACHE.get(key);
		if (cached != null) {
			return cached;
		}

		final FlowingFluidBehavior result = classify(direction, collisionShape, fluidHeight);
		if (SHAPE_CACHE.size() < MAX_CACHE_ENTRIES) {
			SHAPE_CACHE.putIfAbsent(key, result);
		}
		return result;
	}

	static int fluidHeightPixels(final FluidState state) {
		return Math.clamp(state.getAmount() * 2, 0, 14);
	}

	private static FlowingFluidBehavior classify(
			final Direction direction,
			final VoxelShape collisionShape,
			final int fluidHeight
	) {
		for (int slice = 0; slice < VOXELS_PER_AXIS; slice++) {
			final VoxelShape requiredSlice = createRequiredSlice(direction, slice, fluidHeight);
			if (!Shapes.joinIsNotEmpty(requiredSlice, collisionShape, BooleanOp.ONLY_FIRST)) {
				return slice == 0 ? FlowingFluidBehavior.BLOCK : FlowingFluidBehavior.CONTAIN;
			}
		}
		return FlowingFluidBehavior.PASS;
	}

	private static VoxelShape createRequiredSlice(
			final Direction direction,
			final int slice,
			final int fluidHeight
	) {
		final double near = (double) slice / VOXELS_PER_AXIS;
		final double far = (double) (slice + 1) / VOXELS_PER_AXIS;
		final double reversedNear = 1.0 - far;
		final double reversedFar = 1.0 - near;
		final double top = (double) fluidHeight / VOXELS_PER_AXIS;

		return switch (direction) {
			case EAST -> Shapes.box(near, 0.0, 0.0, far, top, 1.0);
			case WEST -> Shapes.box(reversedNear, 0.0, 0.0, reversedFar, top, 1.0);
			case SOUTH -> Shapes.box(0.0, 0.0, near, 1.0, top, far);
			case NORTH -> Shapes.box(0.0, 0.0, reversedNear, 1.0, top, reversedFar);
			default -> throw new IllegalArgumentException("Vertical directions do not use horizontal fluid slices");
		};
	}

	private record ShapeCacheKey(BlockState state, Direction direction, int fluidHeight) {
	}
}
