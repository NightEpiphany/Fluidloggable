package com.moigferdsrte.fluidloggable.flowing;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidFlowBarrierTest {
	static {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void raisedTrapdoorBlocksOnlyItsOccupiedBoundary() {
		final BlockState trapdoor = trapdoor(true, Direction.EAST);

		assertTrue(blocksPassage(Direction.WEST, trapdoor, Blocks.AIR.defaultBlockState()));
		assertTrue(blocksPassage(Direction.EAST, Blocks.AIR.defaultBlockState(), trapdoor));
		assertFalse(blocksPassage(Direction.EAST, trapdoor, Blocks.AIR.defaultBlockState()));
		assertFalse(blocksPassage(Direction.WEST, Blocks.AIR.defaultBlockState(), trapdoor));
	}

	@Test
	void configurationCanDisableTrapdoorBlocking() {
		final BlockState trapdoor = trapdoor(true, Direction.EAST);

		assertTrue(FluidloggableConfig.defaultTrapdoorFluidBlocking());
		assertFalse(FluidFlowBarrier.blocksPassage(
				false,
				Direction.EAST,
				Blocks.AIR.defaultBlockState(),
				trapdoor
		));
	}

	@Test
	void facingIsOppositeToTheRaisedPanelBoundary() {
		final var shape = trapdoor(true, Direction.EAST).getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);

		assertEquals(0.0, shape.min(Direction.Axis.X));
		assertEquals(3.0 / 16.0, shape.max(Direction.Axis.X));
	}

	@Test
	void raisedTrapdoorAllowsFlowAlongItsSides() {
		final BlockState trapdoor = trapdoor(true, Direction.EAST);

		assertFalse(blocksPassage(Direction.NORTH, Blocks.AIR.defaultBlockState(), trapdoor));
		assertFalse(blocksPassage(Direction.SOUTH, Blocks.AIR.defaultBlockState(), trapdoor));
	}

	@Test
	void loweredTrapdoorDoesNotBlockHorizontalFlow() {
		final BlockState trapdoor = trapdoor(false, Direction.EAST);

		for (Direction direction : Direction.Plane.HORIZONTAL) {
			assertFalse(blocksPassage(direction, Blocks.AIR.defaultBlockState(), trapdoor));
		}
	}

	@Test
	void raisedTrapdoorDoesNotChangeVerticalOrUnrelatedBlockPassage() {
		final BlockState trapdoor = trapdoor(true, Direction.EAST);

		assertFalse(blocksPassage(Direction.DOWN, Blocks.AIR.defaultBlockState(), trapdoor));
		assertFalse(blocksPassage(
				Direction.EAST,
				Blocks.AIR.defaultBlockState(),
				Blocks.OAK_SLAB.defaultBlockState()
		));
	}

	private static BlockState trapdoor(final boolean open, final Direction facing) {
		return Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
				.setValue(TrapDoorBlock.OPEN, open)
				.setValue(TrapDoorBlock.FACING, facing);
	}

	private static boolean blocksPassage(
			final Direction direction,
			final BlockState sourceState,
			final BlockState targetState
	) {
		return FluidFlowBarrier.blocksPassage(true, direction, sourceState, targetState);
	}
}
