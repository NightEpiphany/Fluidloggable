package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class DoorFlowGameTest {
	@GameTest(maxTicks = 25)
	public void openingClosedDoorResumesBlockedWaterFlow(final GameTestHelper helper) {
		final DoorSetup setup = prepareDoor(helper, false);

		tickFluid(setup.level(), setup.sourcePos());
		helper.assertTrue(
				setup.level().getFluidState(setup.lowerDoorPos()).isEmpty(),
				"A closed door must initially block water"
		);

		helper.runAfterDelay(10, () -> {
			final BlockState lowerState = setup.level().getBlockState(setup.lowerDoorPos());
			((DoorBlock)lowerState.getBlock()).setOpen(null, setup.level(), lowerState, setup.lowerDoorPos(), true);
		});

		helper.runAfterDelay(24, () -> {
			helper.assertTrue(
					setup.level().getFluidState(setup.lowerDoorPos()).is(FluidTags.WATER),
					"Opening a closed door must wake the blocked water"
			);
			helper.assertTrue(
					setup.level().getFluidState(setup.outputPos()).is(FluidTags.WATER),
					"Water must continue through the open door"
			);
			helper.succeed();
		});
	}

	@GameTest(maxTicks = 1)
	public void breakingLowerWaterloggedDoorPreservesExactFluids(final GameTestHelper helper) {
		assertBreakingDoorPreservesExactFluids(helper, DoubleBlockHalf.LOWER);
	}

	@GameTest(maxTicks = 1)
	public void breakingUpperWaterloggedDoorPreservesExactFluids(final GameTestHelper helper) {
		assertBreakingDoorPreservesExactFluids(helper, DoubleBlockHalf.UPPER);
	}

	private static void assertBreakingDoorPreservesExactFluids(
		final GameTestHelper helper,
		final DoubleBlockHalf brokenHalf
	) {
		final DoorSetup setup = prepareDoor(helper, true);
		final FluidState lowerFluid = Fluids.FLOWING_WATER.getFlowing(5, false);
		final FluidState upperFluid = Fluids.WATER.getSource(false);
		final LevelExtension level = (LevelExtension)setup.level();
		setup.level().setBlock(setup.sourcePos(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.fluidloggable$setFluid(setup.lowerDoorPos(), lowerFluid, Block.UPDATE_ALL);
		level.fluidloggable$setFluid(setup.lowerDoorPos().above(), upperFluid, Block.UPDATE_ALL);

		final BlockPos brokenPos = brokenHalf == DoubleBlockHalf.LOWER
				? setup.lowerDoorPos()
				: setup.lowerDoorPos().above();
		final var player = helper.makeMockPlayer(GameType.CREATIVE);
		final BlockState brokenState = setup.level().getBlockState(brokenPos);
		brokenState.getBlock().playerWillDestroy(setup.level(), brokenPos, brokenState, player);
		setup.level().destroyBlock(brokenPos, false);

		assertExactFluid(helper, setup.lowerDoorPos(), lowerFluid, "lower");
		assertExactFluid(helper, setup.lowerDoorPos().above(), upperFluid, "upper");
		helper.succeed();
	}

	private static void assertExactFluid(
		final GameTestHelper helper,
		final BlockPos pos,
		final FluidState expected,
		final String half
	) {
		final FluidState actual = helper.getLevel().getFluidState(pos);
		helper.assertTrue(
				actual == expected,
				"Breaking a waterlogged door must preserve the exact " + half + " fluid state"
		);
	}

	private static DoorSetup prepareDoor(final GameTestHelper helper, final boolean open) {
		final ServerLevel level = helper.getLevel();
		final BlockPos sourcePos = helper.absolutePos(new BlockPos(2, 2, 3));
		final BlockPos lowerDoorPos = sourcePos.east();
		final BlockPos outputPos = lowerDoorPos.east();
		final BlockState lowerState = Blocks.OAK_DOOR.defaultBlockState()
				.setValue(DoorBlock.FACING, Direction.EAST)
				.setValue(DoorBlock.OPEN, open)
				.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
		final BlockState upperState = lowerState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);

		level.setBlock(lowerDoorPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(lowerDoorPos, lowerState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		level.setBlock(lowerDoorPos.above(), upperState, Block.UPDATE_ALL);
		level.setBlock(sourcePos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(sourcePos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(outputPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			if (direction != Direction.EAST) {
				level.setBlock(sourcePos.relative(direction), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		level.setBlock(lowerDoorPos.north(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(lowerDoorPos.south(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		return new DoorSetup(level, sourcePos, lowerDoorPos, outputPos);
	}

	private static void tickFluid(final ServerLevel level, final BlockPos pos) {
		level.getFluidState(pos).tick(level, pos, level.getBlockState(pos));
	}

	private record DoorSetup(
			ServerLevel level,
			BlockPos sourcePos,
			BlockPos lowerDoorPos,
			BlockPos outputPos
	) {
	}
}
