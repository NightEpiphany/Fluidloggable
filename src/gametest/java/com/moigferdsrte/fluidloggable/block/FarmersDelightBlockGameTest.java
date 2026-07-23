package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import vectorwing.farmersdelight.common.block.TatamiMatBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

public final class FarmersDelightBlockGameTest {
	@GameTest(maxTicks = 1)
	public void tatamiItemPlacesIntoWaterWithoutCreatingWaterInHead(final GameTestHelper helper) {
		final Block tatami = ModBlocks.FULL_TATAMI_MAT.get();
		final BlockPos footPos = helper.absolutePos(new BlockPos(2, 2, 2));
		final BlockPos headPos = footPos.east();
		helper.getLevel().setBlock(footPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		helper.getLevel().setBlock(headPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		helper.getLevel().setBlock(footPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);

		final var player = helper.makeMockPlayer(GameType.CREATIVE);
		player.getAbilities().mayBuild = true;
		player.setYRot(-90.0F);
		final ItemStack stack = new ItemStack(tatami);
		final BlockHitResult hit = new BlockHitResult(
				Vec3.atCenterOf(footPos),
				Direction.UP,
				footPos,
				false
		);
		final BlockPlaceContext context = new BlockPlaceContext(
				helper.getLevel(),
				player,
				InteractionHand.MAIN_HAND,
				stack,
				hit
		);
		final var result = ((BlockItem)tatami.asItem()).place(context);

		helper.assertTrue(result.consumesAction(), "The tatami item must be placed successfully");
		helper.assertTrue(helper.getLevel().getBlockState(footPos).is(tatami), "The foot position must contain tatami");
		helper.assertTrue(helper.getLevel().getBlockState(headPos).is(tatami), "The head position must contain tatami");
		helper.assertTrue(
				helper.getLevel().getFluidState(headPos).isEmpty(),
				"Placing a tatami item into water must not create water in its dry head position"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void placingWaterloggedTatamiDoesNotCreateWaterInDryHead(final GameTestHelper helper) {
		final TatamiSetup setup = prepareTatami(helper, Fluids.EMPTY.defaultFluidState());

		helper.assertTrue(
				setup.level().getFluidState(setup.headPos()).isEmpty(),
				"A waterlogged tatami foot must not create water in a dry head position"
		);
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void placingTatamiPreservesFlowingWaterInHead(final GameTestHelper helper) {
		final FluidState expected = Fluids.FLOWING_WATER.getFlowing(5, false);
		final TatamiSetup setup = prepareTatami(helper, expected);

		helper.assertTrue(
				setup.level().getFluidState(setup.headPos()) == expected,
				"A tatami head must preserve the exact flowing water state it replaces"
		);
		helper.succeed();
	}

	private static TatamiSetup prepareTatami(final GameTestHelper helper, final FluidState headFluid) {
		final Block tatami = ModBlocks.FULL_TATAMI_MAT.get();
		final BlockPos footPos = helper.absolutePos(new BlockPos(2, 2, 2));
		final BlockPos headPos = footPos.east();
		helper.getLevel().setBlock(footPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		helper.getLevel().setBlock(headPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		if (!headFluid.isEmpty()) {
			helper.getLevel().setBlock(headPos, headFluid.createLegacyBlock(), Block.UPDATE_ALL);
		}

		final BlockState footState = tatami.defaultBlockState()
				.setValue(HorizontalDirectionalBlock.FACING, Direction.EAST)
				.setValue(TatamiMatBlock.PART, BedPart.FOOT);
		final LevelExtension level = (LevelExtension)helper.getLevel();
		helper.getLevel().setBlock(footPos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
		level.fluidloggable$setBlockAndInsertFluidIfPossible(footPos, footState, Block.UPDATE_ALL);
		final BlockState placedFoot = helper.getLevel().getBlockState(footPos);
		tatami.setPlacedBy(helper.getLevel(), footPos, placedFoot, null, ItemStack.EMPTY);
		return new TatamiSetup(helper.getLevel(), headPos);
	}

	private record TatamiSetup(net.minecraft.server.level.ServerLevel level, BlockPos headPos) {
	}
}
