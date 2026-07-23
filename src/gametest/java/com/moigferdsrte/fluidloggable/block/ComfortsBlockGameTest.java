package com.moigferdsrte.fluidloggable.block;

import com.illusivesoulworks.comforts.common.ComfortsRegistry;
import com.illusivesoulworks.comforts.common.block.BaseComfortsBlock;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class ComfortsBlockGameTest {
	@GameTest(maxTicks = 1)
	public void breakingSleepingBagFootPreservesBothExactFluids(final GameTestHelper helper) {
		assertBreakingSleepingBagPreservesExactFluids(
				helper,
				BedPart.FOOT,
				Fluids.WATER.getSource(false),
				Fluids.FLOWING_WATER.getFlowing(5, false)
		);
	}

	@GameTest(maxTicks = 1)
	public void breakingSleepingBagHeadPreservesBothExactFluids(final GameTestHelper helper) {
		assertBreakingSleepingBagPreservesExactFluids(
				helper,
				BedPart.HEAD,
				Fluids.FLOWING_WATER.getFlowing(3, false),
				Fluids.WATER.getSource(false)
		);
	}

	private static void assertBreakingSleepingBagPreservesExactFluids(
			final GameTestHelper helper,
			final BedPart brokenPart,
			final FluidState footFluid,
			final FluidState headFluid
	) {
		final Block sleepingBag = ComfortsRegistry.SLEEPING_BAGS.get(DyeColor.GREEN).get();
		final BlockPos footPos = helper.absolutePos(new BlockPos(2, 2, 2));
		final BlockPos headPos = footPos.east();
		final BlockState footState = sleepingBag.defaultBlockState()
				.setValue(BedBlock.FACING, Direction.EAST)
				.setValue(BedBlock.PART, BedPart.FOOT)
				.setValue(BaseComfortsBlock.WATERLOGGED, true);
		final BlockState headState = footState.setValue(BedBlock.PART, BedPart.HEAD);

		helper.getLevel().setBlock(footPos, footState, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		helper.getLevel().setBlock(headPos, headState, Block.UPDATE_ALL);
		final LevelExtension level = (LevelExtension)helper.getLevel();
		level.fluidloggable$setFluid(footPos, footFluid, Block.UPDATE_ALL);
		level.fluidloggable$setFluid(headPos, headFluid, Block.UPDATE_ALL);

		final BlockPos brokenPos = brokenPart == BedPart.FOOT ? footPos : headPos;
		final BlockState brokenState = helper.getLevel().getBlockState(brokenPos);
		final var player = helper.makeMockPlayer(GameType.CREATIVE);
		brokenState.getBlock().playerWillDestroy(helper.getLevel(), brokenPos, brokenState, player);
		helper.getLevel().destroyBlock(brokenPos, false);

		assertExactFluid(helper, footPos, footFluid, "foot");
		assertExactFluid(helper, headPos, headFluid, "head");
		helper.succeed();
	}

	private static void assertExactFluid(
			final GameTestHelper helper,
			final BlockPos pos,
			final FluidState expected,
			final String part
	) {
		final FluidState actual = helper.getLevel().getFluidState(pos);
		helper.assertTrue(
				actual == expected,
				"Breaking a Comforts sleeping bag must preserve the exact " + part + " fluid state"
		);
	}
}
