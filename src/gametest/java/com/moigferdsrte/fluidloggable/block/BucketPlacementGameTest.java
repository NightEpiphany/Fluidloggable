package com.moigferdsrte.fluidloggable.block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public final class BucketPlacementGameTest {
	@GameTest(maxTicks = 1)
	public void lavaBucketFillsAdjacentNonFullBlockPos(final GameTestHelper helper) {
		final ServerLevel level = helper.getLevel();
		final BlockPos targetPos = helper.absolutePos(new BlockPos(4, 2, 3));
		final BlockPos clickedPos = targetPos.east();
		level.setBlock(targetPos.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(targetPos, Blocks.TORCH.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(clickedPos, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);

		final var player = helper.makeMockPlayer(GameType.CREATIVE);
		player.getAbilities().mayBuild = true;
		player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE).setBaseValue(4.5);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LAVA_BUCKET));
		player.setPos(targetPos.getX() - 2.5, targetPos.getY() - 0.82, targetPos.getZ() + 0.5);
		player.setYRot(-90.0F);
		player.setYHeadRot(-90.0F);
		player.setXRot(0.0F);
		helper.assertValueEqual(player.blockInteractionRange(), 4.5, "Mock player block interaction range");
		final var from = player.getEyePosition();
		final var to = from.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange()));
		helper.assertTrue(to.x > from.x + 4.0, "Bucket raycast must point east");
		final BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		helper.assertValueEqual(hit.getBlockPos(), clickedPos, "Bucket raycast block pos");
		helper.assertValueEqual(hit.getDirection(), Direction.WEST, "Bucket raycast face");

		Items.LAVA_BUCKET.use(level, player, InteractionHand.MAIN_HAND);

		helper.assertTrue(level.getBlockState(targetPos).is(Blocks.TORCH), "The adjacent target block must remain present");
		helper.assertTrue(
				LavaloggableBlockSupport.isLavalogged(level.getBlockState(targetPos)),
				"A lava bucket used on the neighbouring face must fill the adjacent non-full block pos"
		);
		helper.succeed();
	}
}
