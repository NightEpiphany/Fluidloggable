package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import com.moigferdsrte.fluidloggable.flowing.FluidFlowUpdateSupport;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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

	@GameTest(maxTicks = 25)
	public void raisedFarSideTrapdoorAcceptsThenStopsWater(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.WEST);

		tickFluid(setup.level(), setup.sourcePos());
		helper.assertTrue(
				setup.level().getFluidState(setup.targetPos()).is(FluidTags.WATER),
				"Water must enter a trapdoor cell when its raised panel is on the far side"
		);
		tickFluid(setup.level(), setup.targetPos());

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(
					setup.level().getFluidState(setup.targetPos()).is(FluidTags.WATER),
					"A containing trapdoor must retain water across fluid ticks"
			);
			helper.assertTrue(
					setup.level().getFluidState(setup.outputPos()).isEmpty(),
					"Water must remain stopped at the far-side panel"
			);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 25)
	public void raisedSideFacingTrapdoorAllowsWater(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.NORTH);

		tickFluid(setup.level(), setup.sourcePos());
		tickFluid(setup.level(), setup.targetPos());

		helper.runAfterDelay(20, () -> {
			helper.assertTrue(
					setup.level().getFluidState(setup.targetPos()).is(FluidTags.WATER),
					"A passable trapdoor must retain its flowing water state"
			);
			helper.assertTrue(
					setup.level().getFluidState(setup.outputPos()).is(FluidTags.WATER),
					"Water must keep passing along the side of a raised trapdoor"
			);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 25)
	public void loweredTrapdoorPassesWaterWithoutOscillation(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), false, Direction.EAST);

		tickFluid(setup.level(), setup.sourcePos());
		helper.runAfterDelay(20, () -> {
			helper.assertTrue(
					setup.level().getFluidState(setup.targetPos()).is(FluidTags.WATER),
					"A lower collision shape must retain its passing water"
			);
			helper.assertTrue(
					setup.level().getFluidState(setup.outputPos()).is(FluidTags.WATER),
					"Water above a lowered trapdoor must keep passing without oscillation"
			);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 1)
	public void disabledBlockingAllowsWaterThroughRaisedNearSideTrapdoor(final GameTestHelper helper) {
		final boolean previous = FluidloggableConfig.isCollisionShapeFluidBlockingEnabled();
		try {
			FluidloggableConfig.setCollisionShapeFluidBlockingEnabled(false);
			final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.EAST);

			tickFluid(setup.level(), setup.sourcePos());

			helper.assertTrue(
					setup.level().getFluidState(setup.targetPos()).is(FluidTags.WATER),
					"Disabled trapdoor blocking must allow water into a raised near-side trapdoor"
			);
			helper.succeed();
		} finally {
			FluidloggableConfig.setCollisionShapeFluidBlockingEnabled(previous);
		}
	}

	@GameTest(maxTicks = 20)
	public void closingTrapdoorResumesBlockedWaterFlow(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.EAST);
		final BlockPos trapdoorPos = setup.targetPos();
		final BlockState closed = setup.level().getBlockState(trapdoorPos).setValue(TrapDoorBlock.OPEN, false);

		setup.level().setBlock(trapdoorPos, closed, Block.UPDATE_CLIENTS);
		FluidFlowUpdateSupport.scheduleAdjacentFluidTicks(setup.level(), trapdoorPos);
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(
					setup.level().getFluidState(setup.outputPos()).is(FluidTags.WATER),
					"Closing a trapdoor must resume water flow through its cell"
			);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 20)
	public void playerInteractionResumesBlockedWaterFlow(final GameTestHelper helper) {
		final FlowSetup setup = prepareFlow(helper, Blocks.WATER.defaultBlockState(), true, Direction.EAST);
		final var player = helper.makeMockPlayer(GameType.CREATIVE);
		final BlockHitResult hit = new BlockHitResult(
				Vec3.atCenterOf(setup.targetPos()),
				Direction.UP,
				setup.targetPos(),
				false
		);
		final InteractionResult result = setup.level().getBlockState(setup.targetPos()).useWithoutItem(setup.level(), player, hit);

		helper.assertTrue(result == InteractionResult.SUCCESS, "Trapdoor interaction must succeed");
		helper.runAfterDelay(10, () -> {
			helper.assertTrue(
					setup.level().getFluidState(setup.outputPos()).is(FluidTags.WATER),
					"Player interaction must resume water flow after closing a trapdoor"
			);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 20)
	public void denseWaterloggedTrapdoorsDoNotChainNeighbourUpdates(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		for (int x = 1; x <= 5; x++) {
			for (int z = 1; z <= 5; z++) {
				final BlockPos pos = helper.absolutePos(new BlockPos(x, 2, z));
				level.setBlock(
						pos,
						Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
								.setValue(TrapDoorBlock.OPEN, true)
								.setValue(TrapDoorBlock.FACING, Direction.NORTH)
								.setValue(TrapDoorBlock.WATERLOGGED, true),
						Block.UPDATE_ALL
				);
			}
		}

		for (int x = 1; x <= 5; x++) {
			for (int z = 1; z <= 5; z++) {
				level.updateNeighborsAt(helper.absolutePos(new BlockPos(x, 2, z)), Blocks.SPRUCE_TRAPDOOR);
			}
		}
		helper.runAfterDelay(10, helper::succeed);
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
