package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class LavaloggedFlowGameTest {
	@GameTest(maxTicks = 1)
	public void lavaSourceFlowsOutOfFullCollisionLavaloggedBlock(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		final BlockPos outputPos = channelPos.east();
		closeHorizontalSides(level, channelPos, Direction.EAST);
		level.setBlock(outputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(channelPos, beaconChannel(), Block.UPDATE_ALL);
		final FluidState source = Fluids.LAVA.getSource(false);
		setStoredFluid(level, channelPos, source);

		source.tick(level, channelPos, level.getBlockState(channelPos));

		helper.assertTrue(level.getBlockState(channelPos).is(Blocks.BEACON), "The containing beacon must remain present");
		helper.assertTrue(
				level.getFluidState(outputPos).is(FluidTags.LAVA),
				"A stored lava source must flow out through its full-collision containing block"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void supportedFlowingLavaUpdatesAndFlowsOutOfFullCollisionBlock(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		final BlockPos sourcePos = channelPos.west();
		final BlockPos outputPos = channelPos.east();
		level.setBlock(channelPos, beaconChannel(), Block.UPDATE_ALL);
		level.setBlock(channelPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(channelPos.north(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(channelPos.south(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(outputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		final FluidState flowing = Fluids.FLOWING_LAVA.getFlowing(1, false);
		setStoredFluid(level, channelPos, flowing);

		flowing.tick(level, channelPos, level.getBlockState(channelPos));

		helper.assertTrue(
				level.getFluidState(channelPos).getAmount() > flowing.getAmount(),
				"A neighbouring source must update flowing lava inside a full-collision block"
		);
		helper.assertTrue(
				level.getFluidState(outputPos).is(FluidTags.LAVA),
				"Supported flowing lava must continue flowing out of its containing block"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void storedLavaTickPreservesChannelAndUpdatesLevel(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		final BlockPos sourcePos = channelPos.west();
		closeHorizontalSides(level, channelPos, Direction.WEST);
		level.setBlock(sourcePos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(channelPos, channel(false, true), Block.UPDATE_ALL);

		final FluidState initial = Fluids.FLOWING_LAVA.getFlowing(1, false);
		setStoredFluid(level, channelPos, initial);
		initial.tick(level, channelPos, level.getBlockState(channelPos));

		helper.assertTrue(
				level.getBlockState(channelPos).is(FluidloggedGameTestBootstrap.testBlock),
				"Stored lava must not replace its containing block"
		);
		helper.assertTrue(level.getFluidState(channelPos).is(FluidTags.LAVA), "The channel must retain lava");
		helper.assertTrue(
				level.getFluidState(channelPos).getAmount() > initial.getAmount(),
				"The source neighbour must update the stored flowing level"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void storedLavaFlowsOutOfChannel(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		final BlockPos outputPos = channelPos.east();
		closeHorizontalSides(level, channelPos, Direction.EAST);
		level.setBlock(outputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(channelPos, channel(false, true), Block.UPDATE_ALL);

		final FluidState source = Fluids.LAVA.getSource(false);
		setStoredFluid(level, channelPos, source);
		source.tick(level, channelPos, level.getBlockState(channelPos));

		helper.assertTrue(level.getFluidState(outputPos).is(FluidTags.LAVA), "Stored lava must flow out of the channel");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void storedLavaFlowsBetweenLavaloggedChannels(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = absolute(helper, 2, 2, 3);
		final BlockPos targetPos = sourcePos.east();
		closeHorizontalSides(level, sourcePos, Direction.EAST);
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos, channel(false, true), Block.UPDATE_ALL);
		level.setBlock(targetPos, channel(false, true), Block.UPDATE_ALL);
		final FluidState source = Fluids.LAVA.getSource(false);
		final FluidState target = Fluids.FLOWING_LAVA.getFlowing(1, false);
		setStoredFluid(level, sourcePos, source);
		setStoredFluid(level, targetPos, target);

		source.tick(level, sourcePos, level.getBlockState(sourcePos));

		helper.assertTrue(
				level.getFluidState(targetPos).getAmount() > target.getAmount(),
				"A lavalogged source must update an adjacent lavalogged flow"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void storedLavaFlowsDownIntoLavaloggedChannel(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = absolute(helper, 3, 3, 3);
		final BlockPos targetPos = sourcePos.below();
		level.setBlock(sourcePos, channel(false, true), Block.UPDATE_ALL);
		level.setBlock(targetPos, channel(false, true), Block.UPDATE_ALL);
		final FluidState source = Fluids.LAVA.getSource(false);
		final FluidState target = Fluids.FLOWING_LAVA.getFlowing(1, false);
		setStoredFluid(level, sourcePos, source);
		setStoredFluid(level, targetPos, target);

		source.tick(level, sourcePos, level.getBlockState(sourcePos));

		helper.assertTrue(
				level.getFluidState(targetPos).getAmount() > target.getAmount(),
				"A lavalogged source must update a flowing lavalogged channel below"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void adjacentLavaloggedSourceRemainsSource(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = absolute(helper, 2, 2, 3);
		final BlockPos targetPos = sourcePos.east();
		closeHorizontalSides(level, sourcePos, Direction.EAST);
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos, channel(false, true), Block.UPDATE_ALL);
		level.setBlock(targetPos, channel(false, true), Block.UPDATE_ALL);
		final FluidState source = Fluids.LAVA.getSource(false);
		setStoredFluid(level, sourcePos, source);
		setStoredFluid(level, targetPos, source);

		source.tick(level, sourcePos, level.getBlockState(sourcePos));

		helper.assertTrue(level.getFluidState(targetPos).isSource(), "A lavalogged source must remain a source");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void sameLavaReplacementOnlyAllowsFlowingState(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		level.setBlock(channelPos, channel(false, true), Block.UPDATE_ALL);
		final FluidState source = Fluids.LAVA.getSource(false);
		final FluidState flowing = Fluids.FLOWING_LAVA.getFlowing(1, false);

		setStoredFluid(level, channelPos, source);
		helper.assertFalse(
				source.canBeReplacedWith(level, channelPos, Fluids.LAVA, Direction.EAST),
				"Stored source lava must reject same-lava replacement"
		);
		setStoredFluid(level, channelPos, flowing);
		helper.assertTrue(
				flowing.canBeReplacedWith(level, channelPos, Fluids.LAVA, Direction.EAST),
				"Stored flowing lava must allow same-lava replacement"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void naturalLavaFlowsThroughDryFence(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = absolute(helper, 2, 2, 3);
		final BlockPos targetPos = sourcePos.east();
		final BlockPos outputPos = targetPos.east();
		prepareSourceWithSingleOpening(level, sourcePos, targetPos, Blocks.OAK_FENCE.defaultBlockState());
		level.setBlock(outputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.north(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.south(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

		level.getFluidState(sourcePos).tick(level, sourcePos, level.getBlockState(sourcePos));

		helper.assertTrue(
				level.getBlockState(targetPos).is(Blocks.OAK_FENCE),
				"Natural lava must not replace a dry fence"
		);
		helper.assertTrue(
				LavaloggableBlockSupport.isLavalogged(level.getBlockState(targetPos)),
				"Natural lava must fill a dry lavaloggable fence"
		);
		final FluidState storedLava = level.getFluidState(targetPos);
		helper.assertTrue(storedLava.is(FluidTags.LAVA), "The fence must contain flowing lava");
		storedLava.tick(level, targetPos, level.getBlockState(targetPos));
		helper.assertTrue(level.getFluidState(outputPos).is(FluidTags.LAVA), "Lava must continue flowing past the fence");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void naturalWaterFlowsThroughDryFence(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = absolute(helper, 2, 2, 3);
		final BlockPos targetPos = sourcePos.east();
		final BlockPos outputPos = targetPos.east();
		level.setBlock(sourcePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos, Blocks.OAK_FENCE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(outputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.north(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.south(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!sourcePos.relative(direction).equals(targetPos)) {
				level.setBlock(sourcePos.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}

		level.getFluidState(sourcePos).tick(level, sourcePos, level.getBlockState(sourcePos));

		helper.assertTrue(level.getBlockState(targetPos).is(Blocks.OAK_FENCE), "Natural water must not replace a dry fence");
		helper.assertTrue(
				WaterloggableBlockSupport.isWaterlogged(level.getBlockState(targetPos)),
				"Natural water must fill a dry waterloggable fence"
		);
		final FluidState storedWater = level.getFluidState(targetPos);
		helper.assertTrue(storedWater.is(FluidTags.WATER), "The fence must contain flowing water");
		storedWater.tick(level, targetPos, level.getBlockState(targetPos));
		helper.assertTrue(level.getFluidState(outputPos).is(FluidTags.WATER), "Water must continue flowing past the fence");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void naturalLavaDoesNotReplaceWaterloggedChannel(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = absolute(helper, 2, 2, 3);
		final BlockPos targetPos = sourcePos.east();
		prepareSourceWithSingleOpening(level, sourcePos, targetPos, channel(true, false));
		setStoredFluid(level, targetPos, Fluids.WATER.getSource(false));

		level.getFluidState(sourcePos).tick(level, sourcePos, level.getBlockState(sourcePos));

		helper.assertTrue(
				WaterloggableBlockSupport.isWaterlogged(level.getBlockState(targetPos)),
				"Natural lava must not replace stored water"
		);
		helper.assertTrue(level.getFluidState(targetPos).is(FluidTags.WATER), "The target must retain water");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void naturalWaterDoesNotReplaceLavaloggedChannel(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = absolute(helper, 2, 2, 3);
		final BlockPos targetPos = sourcePos.east();
		level.setBlock(sourcePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos, channel(false, true), Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!sourcePos.relative(direction).equals(targetPos)) {
				level.setBlock(sourcePos.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		setStoredFluid(level, targetPos, Fluids.LAVA.getSource(false));

		level.getFluidState(sourcePos).tick(level, sourcePos, level.getBlockState(sourcePos));

		helper.assertTrue(
				LavaloggableBlockSupport.isLavalogged(level.getBlockState(targetPos)),
				"Natural water must not replace stored lava"
		);
		helper.assertTrue(level.getFluidState(targetPos).is(FluidTags.LAVA), "The target must retain lava");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void fallingLavaUsesVanillaWaterContactBeforeSideSpread(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos lavaPos = absolute(helper, 3, 3, 3);
		final BlockPos waterPos = lavaPos.below();
		final BlockPos sideOutputPos = lavaPos.east();
		level.setBlock(lavaPos, channel(false, true), Block.UPDATE_ALL);
		level.setBlock(waterPos, channel(true, false), Block.UPDATE_ALL);
		level.setBlock(sideOutputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (direction != Direction.EAST) {
				level.setBlock(lavaPos.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		final FluidState lava = Fluids.LAVA.getSource(false);
		setStoredFluid(level, lavaPos, lava);
		setStoredFluid(level, waterPos, Fluids.WATER.getSource(false));

		lava.tick(level, lavaPos, level.getBlockState(lavaPos));

		helper.assertTrue(level.getFluidState(waterPos).is(FluidTags.WATER), "Vanilla water contact must retain stored water");
		helper.assertTrue(
				level.getFluidState(sideOutputPos).isEmpty(),
				"Downward lava-water contact must run before horizontal spreading"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void dryChannelBelowAcceptsDownwardLava(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos flowPos = absolute(helper, 3, 3, 3);
		final BlockPos sourcePos = flowPos.west();
		final BlockPos dryBelowPos = flowPos.below();
		final BlockPos sideOutputPos = flowPos.east();
		level.setBlock(sourcePos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(flowPos, channel(false, true), Block.UPDATE_ALL);
		level.setBlock(dryBelowPos, channel(false, false), Block.UPDATE_ALL);
		level.setBlock(sideOutputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(flowPos.north(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(flowPos.south(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		final FluidState flowing = Fluids.FLOWING_LAVA.getFlowing(6, false);
		setStoredFluid(level, flowPos, flowing);

		flowing.tick(level, flowPos, level.getBlockState(flowPos));

		helper.assertTrue(level.getFluidState(dryBelowPos).is(FluidTags.LAVA), "A dry channel below must accept downward lava");
		helper.assertTrue(
				level.getFluidState(sideOutputPos).isEmpty(),
				"Downward lava flow must retain vanilla priority over horizontal spreading"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void unsupportedFlowingLavaDecayPreservesChannel(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		closeHorizontalSides(level, channelPos, null);
		level.setBlock(channelPos, channel(false, true), Block.UPDATE_ALL);

		final FluidState flowing = Fluids.FLOWING_LAVA.getFlowing(1, false);
		setStoredFluid(level, channelPos, flowing);
		flowing.tick(level, channelPos, level.getBlockState(channelPos));

		helper.assertTrue(
				level.getBlockState(channelPos).is(FluidloggedGameTestBootstrap.testBlock),
				"Lava decay must preserve the containing block"
		);
		helper.assertFalse(
				LavaloggableBlockSupport.isLavalogged(level.getBlockState(channelPos)),
				"Lava decay must clear LAVALOGGED"
		);
		helper.assertTrue(level.getFluidState(channelPos).isEmpty(), "Decayed lava must be removed");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void exactFallingLavaStateRoundTrips(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		level.setBlock(channelPos, channel(false, true), Block.UPDATE_ALL);
		final FluidState falling = Fluids.FLOWING_LAVA.getFlowing(8, true);

		setStoredFluid(level, channelPos, falling);

		helper.assertValueEqual(level.getFluidState(channelPos), falling, "Stored falling lava state");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void storedWaterTickStillPreservesChannel(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos channelPos = absolute(helper, 3, 2, 3);
		final BlockPos sourcePos = channelPos.west();
		closeHorizontalSides(level, channelPos, Direction.WEST);
		level.setBlock(sourcePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(channelPos, channel(true, false), Block.UPDATE_ALL);

		final FluidState flowing = Fluids.FLOWING_WATER.getFlowing(1, false);
		setStoredFluid(level, channelPos, flowing);
		flowing.tick(level, channelPos, level.getBlockState(channelPos));

		helper.assertTrue(
				level.getBlockState(channelPos).is(FluidloggedGameTestBootstrap.testBlock),
				"The water flow path must keep preserving its containing block"
		);
		helper.assertTrue(level.getFluidState(channelPos).is(FluidTags.WATER), "The channel must retain water");
		helper.assertTrue(
				level.getFluidState(channelPos).getAmount() > flowing.getAmount(),
				"The source neighbour must update the stored water level"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void lavaloggedStateEmitsLavaLight(final GameTestHelper helper) {
		helper.assertValueEqual(channel(false, true).getLightEmission(), 15, "Lavalogged light emission");
		helper.succeed();
	}

	private static BlockState channel(final boolean waterlogged, final boolean lavalogged) {
		return FluidloggedGameTestBootstrap.testBlock.defaultBlockState()
				.setValue(BlockStateProperties.WATERLOGGED, waterlogged)
				.setValue(LavaloggableBlockSupport.LAVALOGGED, lavalogged)
				.setValue(BlockStateProperties.POWERED, false);
	}

	private static BlockState beaconChannel() {
		return Blocks.BEACON.defaultBlockState()
				.setValue(BlockStateProperties.WATERLOGGED, false)
				.setValue(LavaloggableBlockSupport.LAVALOGGED, true);
	}

	private static void setStoredFluid(
			final ServerLevel level,
			final BlockPos pos,
			final FluidState fluidState
	) {
		((LevelExtension) level).fluidloggable$setFluid(
				pos,
				fluidState,
				Block.UPDATE_ALL | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK
		);
	}

	private static void prepareSourceWithSingleOpening(
			final ServerLevel level,
			final BlockPos sourcePos,
			final BlockPos targetPos,
			final BlockState targetState
	) {
		level.setBlock(sourcePos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos, targetState, Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (!sourcePos.relative(direction).equals(targetPos)) {
				level.setBlock(sourcePos.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
	}

	private static void closeHorizontalSides(
			final ServerLevel level,
			final BlockPos center,
			final Direction openDirection
	) {
		level.setBlock(center.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (direction != openDirection) {
				level.setBlock(center.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
	}

	private static BlockPos absolute(final GameTestHelper helper, final int x, final int y, final int z) {
		return helper.absolutePos(new BlockPos(x, y, z));
	}
}
