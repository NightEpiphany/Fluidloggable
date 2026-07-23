package com.moigferdsrte.fluidloggable.flowing;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidFlowBarrierTest {
	private static final BlockPos SOURCE_POS = BlockPos.ZERO;
	private static final BlockPos TARGET_POS = SOURCE_POS.east();

	static {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private static final FluidState SOURCE_WATER = Fluids.WATER.getSource(false);

	@Test
	void firstCompleteSliceBlocksFluidBeforeItEnters() {
		assertEquals(
				FlowingFluidBehavior.BLOCK,
				behavior(Direction.EAST, trapdoor(true, Direction.EAST), SOURCE_WATER)
		);
	}

	@Test
	void laterCompleteSliceContainsFluidInsideBlock() {
		assertEquals(
				FlowingFluidBehavior.CONTAIN,
				behavior(Direction.EAST, trapdoor(true, Direction.WEST), SOURCE_WATER)
		);
	}

	@Test
	void incompleteSlicesAllowFluidToPass() {
		assertEquals(
				FlowingFluidBehavior.PASS,
				behavior(Direction.EAST, trapdoor(true, Direction.NORTH), SOURCE_WATER)
		);
		assertEquals(
				FlowingFluidBehavior.PASS,
				behavior(Direction.EAST, Blocks.VINE.defaultBlockState(), SOURCE_WATER)
		);
	}

	@Test
	void collisionMustReachCurrentFluidHeight() {
		final BlockState slab = Blocks.STONE_SLAB.defaultBlockState();

		assertEquals(
				FlowingFluidBehavior.BLOCK,
				behavior(Direction.EAST, slab, Fluids.FLOWING_WATER.getFlowing(4, false))
		);
		assertEquals(FlowingFluidBehavior.PASS, behavior(Direction.EAST, slab, SOURCE_WATER));
	}

	@Test
	void passShapeRemainsPassAtEveryFluidHeight() {
		final BlockState sideFacingTrapdoor = trapdoor(true, Direction.NORTH);

		assertEquals(FlowingFluidBehavior.PASS, behavior(Direction.EAST, sideFacingTrapdoor, SOURCE_WATER));
		assertEquals(
				FlowingFluidBehavior.PASS,
				behavior(Direction.EAST, sideFacingTrapdoor, Fluids.FLOWING_WATER.getFlowing(1, false))
		);
	}

	@Test
	void perpendicularFenceConnectionsContainFluidAfterEntry() {
		final BlockState fence = Blocks.OAK_FENCE.defaultBlockState()
				.setValue(CrossCollisionBlock.NORTH, true)
				.setValue(CrossCollisionBlock.SOUTH, true);

		assertEquals(FlowingFluidBehavior.CONTAIN, behavior(Direction.EAST, fence, SOURCE_WATER));
	}

	@Test
	void parallelFenceConnectionDoesNotFormCompleteSlice() {
		final BlockState fence = Blocks.OAK_FENCE.defaultBlockState()
				.setValue(CrossCollisionBlock.EAST, true);

		assertEquals(FlowingFluidBehavior.PASS, behavior(Direction.EAST, fence, SOURCE_WATER));
	}

	@Test
	void verticalFlowKeepsVanillaPassageBehavior() {
		assertEquals(
				FlowingFluidBehavior.PASS,
				behavior(Direction.DOWN, Blocks.STONE.defaultBlockState(), SOURCE_WATER)
		);
	}

	@Test
	void fluidHeightUsesTwoPixelSteps() {
		assertEquals(14, FluidFlowBarrier.fluidHeightPixels(SOURCE_WATER));
		assertEquals(14, FluidFlowBarrier.fluidHeightPixels(Fluids.FLOWING_WATER.getFlowing(7, false)));
		assertEquals(10, FluidFlowBarrier.fluidHeightPixels(Fluids.FLOWING_WATER.getFlowing(5, false)));
		assertEquals(4, FluidFlowBarrier.fluidHeightPixels(Fluids.FLOWING_WATER.getFlowing(2, false)));
		assertEquals(2, FluidFlowBarrier.fluidHeightPixels(Fluids.FLOWING_WATER.getFlowing(1, false)));
	}

	@Test
	void sourceExitUsesOutgoingFaceWhileTargetUsesIncomingFace() {
		final BlockState sourcePanelBehindFlow = trapdoor(true, Direction.EAST);
		final BlockState targetFarSidePanel = trapdoor(true, Direction.WEST);

		assertFalse(blocksPassage(Blocks.AIR.defaultBlockState(), targetFarSidePanel));
		assertFalse(blocksPassage(sourcePanelBehindFlow, Blocks.AIR.defaultBlockState()));
		assertFalse(FluidFlowBarrier.blocksSourceExit(
				true,
				Direction.EAST,
				EmptyBlockGetter.INSTANCE,
				SOURCE_POS,
				sourcePanelBehindFlow,
				SOURCE_WATER
		));
		assertTrue(FluidFlowBarrier.blocksSourceExit(
				true,
				Direction.EAST,
				EmptyBlockGetter.INSTANCE,
				SOURCE_POS,
				sourcePanelBehindFlow,
				Fluids.FLOWING_WATER.getFlowing(7, false)
		));
		assertTrue(FluidFlowBarrier.blocksSourceExit(
				true,
				Direction.EAST,
				EmptyBlockGetter.INSTANCE,
				SOURCE_POS,
				targetFarSidePanel,
				SOURCE_WATER
		));
		assertFalse(FluidFlowBarrier.blocksTargetEntry(
				true,
				Direction.EAST,
				EmptyBlockGetter.INSTANCE,
				TARGET_POS,
				targetFarSidePanel,
				SOURCE_WATER
		));
	}

	@Test
	void configurationCanDisableShapeBlocking() {
		assertFalse(FluidFlowBarrier.blocksPassage(
				false,
				Direction.EAST,
				EmptyBlockGetter.INSTANCE,
				SOURCE_POS,
				Blocks.AIR.defaultBlockState(),
				TARGET_POS,
				Blocks.STONE.defaultBlockState(),
				SOURCE_WATER
		));
	}

	private static FlowingFluidBehavior behavior(
			final Direction direction,
			final BlockState state,
			final FluidState fluidState
	) {
		return FluidFlowBarrier.behavior(
				direction,
				EmptyBlockGetter.INSTANCE,
				BlockPos.ZERO,
				state,
				fluidState
		);
	}

	private static BlockState trapdoor(final boolean open, final Direction facing) {
		return Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
				.setValue(TrapDoorBlock.OPEN, open)
				.setValue(TrapDoorBlock.FACING, facing);
	}

	private static boolean blocksPassage(final BlockState sourceState, final BlockState targetState) {
		return blocksPassage(sourceState, targetState, SOURCE_WATER);
	}

	private static boolean blocksPassage(
			final BlockState sourceState,
			final BlockState targetState,
			final FluidState fluidState
	) {
		return FluidFlowBarrier.blocksPassage(
				true,
				Direction.EAST,
				EmptyBlockGetter.INSTANCE,
				SOURCE_POS,
				sourceState,
				TARGET_POS,
				targetState,
				fluidState
		);
	}
}
