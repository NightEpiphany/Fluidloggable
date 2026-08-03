package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

public final class TaggedFluidloggableBlockGameTest {
	@GameTest(maxTicks = 1)
	public void configuredBlockStoresExactWaterAndLavaWithStateProperties(final GameTestHelper helper) {
		helper.assertFalse(
				ConfiguredFluidloggableBlockSupport.isConfigured(FluidloggedGameTestBootstrap.lateConfiguredBlock.defaultBlockState()),
				"Blocks configured after registration must remain disabled until restart"
		);
		final BlockPos pos = helper.absolutePos(new BlockPos(3, 2, 3));
		final var level = helper.getLevel();
		level.setBlock(pos, FluidloggedGameTestBootstrap.taggedBlock.defaultBlockState(), Block.UPDATE_ALL);

		final BlockState dryState = level.getBlockState(pos);
		helper.assertTrue(dryState.hasProperty(WaterloggableBlockSupport.WATERLOGGED), "Configured blocks must define WATERLOGGED");
		helper.assertTrue(dryState.hasProperty(LavaloggableBlockSupport.LAVALOGGED), "Configured blocks must define LAVALOGGED");
		helper.assertFalse(dryState.getValue(WaterloggableBlockSupport.WATERLOGGED), "Configured blocks must default to dry water state");
		helper.assertFalse(dryState.getValue(LavaloggableBlockSupport.LAVALOGGED), "Configured blocks must default to dry lava state");

		final FluidState flowingWater = Fluids.FLOWING_WATER.getFlowing(4, false);
		((LevelExtension) level).fluidloggable$setFluid(pos, flowingWater, Block.UPDATE_ALL);
		helper.assertTrue(level.getBlockState(pos).is(FluidloggedGameTestBootstrap.taggedBlock), "Water must preserve the block");
		helper.assertTrue(level.getBlockState(pos).getValue(WaterloggableBlockSupport.WATERLOGGED), "Stored water must sync WATERLOGGED");
		helper.assertFalse(level.getBlockState(pos).getValue(LavaloggableBlockSupport.LAVALOGGED), "Stored water must clear LAVALOGGED");
		helper.assertTrue(level.getFluidState(pos) == flowingWater, "Tag block must retain the exact flowing water state");

		final FluidState flowingLava = Fluids.FLOWING_LAVA.getFlowing(3, false);
		((LevelExtension) level).fluidloggable$setFluid(pos, flowingLava, Block.UPDATE_ALL);
		helper.assertTrue(level.getBlockState(pos).is(FluidloggedGameTestBootstrap.taggedBlock), "Lava must preserve the block");
		helper.assertFalse(level.getBlockState(pos).getValue(WaterloggableBlockSupport.WATERLOGGED), "Stored lava must clear WATERLOGGED");
		helper.assertTrue(level.getBlockState(pos).getValue(LavaloggableBlockSupport.LAVALOGGED), "Stored lava must sync LAVALOGGED");
		helper.assertTrue(level.getFluidState(pos) == flowingLava, "Tag block must retain the exact flowing lava state");
		helper.succeed();
	}

	@GameTest(maxTicks = 1)
	public void bucketFillsTaggedBlockWithoutOverwritingStoredFluid(final GameTestHelper helper) {
		final BlockPos targetPos = helper.absolutePos(new BlockPos(4, 2, 3));
		final var level = helper.getLevel();
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos, FluidloggedGameTestBootstrap.taggedBlock.defaultBlockState(), Block.UPDATE_ALL);

		final var player = helper.makeMockPlayer(GameType.CREATIVE);
		player.getAbilities().mayBuild = true;
		player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(4.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
		player.setPos(targetPos.getX() - 2.5, targetPos.getY() - 0.82, targetPos.getZ() + 0.5);
		player.setYRot(-90.0F);
		player.setYHeadRot(-90.0F);
		player.setXRot(0.0F);
		final var from = player.getEyePosition();
		final var to = from.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange()));
		final BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		helper.assertValueEqual(hit.getBlockPos(), targetPos, "Bucket raycast must hit the tagged block");

		Items.WATER_BUCKET.use(level, player, InteractionHand.MAIN_HAND);
		helper.assertTrue(level.getBlockState(targetPos).is(FluidloggedGameTestBootstrap.taggedBlock), "Water must preserve the tagged block");
		helper.assertTrue(level.getFluidState(targetPos).is(net.minecraft.tags.FluidTags.WATER), "Water bucket must fill the tagged block");

		final FluidState storedWater = level.getFluidState(targetPos);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
		Items.LAVA_BUCKET.use(level, player, InteractionHand.MAIN_HAND);
		helper.assertTrue(level.getFluidState(targetPos) == storedWater, "A second bucket must not overwrite stored water");
		helper.succeed();
	}
}
