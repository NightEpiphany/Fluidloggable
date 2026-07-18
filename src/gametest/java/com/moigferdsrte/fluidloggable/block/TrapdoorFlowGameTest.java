package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class TrapdoorFlowGameTest {
	@GameTest(maxTicks = 1)
	public void raisedNearSideTrapdoorBlocksWater(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.EAST);

		tickFluid(setup.level(), setup.sourcePos());

		helper.assertTrue(setup.level().getFluidState(setup.targetPos()).isEmpty(), "Raised near-side trapdoor must block water");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void raisedNearSideTrapdoorBlocksLava(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.LAVA.defaultBlockState(), true, Direction.EAST);

		tickFluid(setup.level(), setup.sourcePos());

		helper.assertTrue(setup.level().getFluidState(setup.targetPos()).isEmpty(), "Raised near-side trapdoor must block lava");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void raisedFarSideTrapdoorAcceptsThenStopsWater(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.WEST);

		tickFluid(setup.level(), setup.sourcePos());
		helper.assertTrue(
				setup.level().getFluidState(setup.targetPos()).is(FluidTags.WATER),
				"Water must enter a trapdoor cell when its raised panel is on the far side"
		);
		tickFluid(setup.level(), setup.targetPos());

		helper.assertTrue(
				setup.level().getFluidState(setup.outputPos()).isEmpty(),
				"Water must stop at the far-side panel of a raised trapdoor"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void raisedSideFacingTrapdoorAllowsWater(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.NORTH);

		tickFluid(setup.level(), setup.sourcePos());
		tickFluid(setup.level(), setup.targetPos());

		helper.assertTrue(
				setup.level().getFluidState(setup.outputPos()).is(FluidTags.WATER),
				"Water must pass completely along the side of a raised trapdoor"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void loweredTrapdoorAllowsWater(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), false, Direction.EAST);

		tickFluid(setup.level(), setup.sourcePos());
		tickFluid(setup.level(), setup.targetPos());

		helper.assertTrue(
				setup.level().getFluidState(setup.outputPos()).is(FluidTags.WATER),
				"Water must pass completely through a lowered trapdoor horizontally"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void disabledBlockingAllowsWaterThroughRaisedNearSideTrapdoor(final GameTestHelper helper) {
		final boolean previous = FluidloggableConfig.isTrapdoorFluidBlockingEnabled();
		try {
			FluidloggableConfig.setTrapdoorFluidBlockingEnabled(false);
			final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.EAST);

			tickFluid(setup.level(), setup.sourcePos());

			helper.assertTrue(
					setup.level().getFluidState(setup.targetPos()).is(FluidTags.WATER),
					"Disabled trapdoor blocking must allow water into a raised near-side trapdoor"
			);
			helper.succeed();
		} finally {
			FluidloggableConfig.setTrapdoorFluidBlockingEnabled(previous);
		}
	}

	private static FlowSetup prepareFlow(
			final GameTestHelper helper,
			final BlockState sourceState,
			final boolean open,
			final Direction facing
	) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = helper.absolutePos(new BlockPos(2, 2, 3));
		final BlockPos targetPos = sourcePos.east();
		final BlockPos outputPos = targetPos.east();
		level.setBlock(sourcePos, sourceState, Block.UPDATE_ALL);
		level.setBlock(
				targetPos,
				Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
						.setValue(TrapDoorBlock.OPEN, open)
						.setValue(TrapDoorBlock.FACING, facing),
				Block.UPDATE_ALL
		);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(outputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (direction != Direction.EAST) {
				level.setBlock(sourcePos.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		level.setBlock(targetPos.north(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.south(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		return new FlowSetup(level, sourcePos, targetPos, outputPos);
	}

	private static void tickFluid(final ServerLevel level, final BlockPos pos) {
		level.getFluidState(pos).tick(level, pos, level.getBlockState(pos));
	}

	private record FlowSetup(ServerLevel level, BlockPos sourcePos, BlockPos targetPos, BlockPos outputPos) {
	}
}
