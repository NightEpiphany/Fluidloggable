package com.moigferdsrte.fluidloggable.block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockDefaultStateGameTest {
	@GameTest
	public void everyDefaultRegistrationClearsOnlyLavalogged(final GameTestHelper helper) {
		final BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState();
		helper.assertTrue(leaves.hasProperty(LavaloggableBlockSupport.LAVALOGGED), "Leaves must support lavalogging");
		helper.assertFalse(
				leaves.getValue(LavaloggableBlockSupport.LAVALOGGED),
				"Leaves must not contain lava by default"
		);
		helper.assertFalse(leaves.getValue(LeavesBlock.WATERLOGGED), "Leaves must preserve their dry water default");
		helper.assertValueEqual(leaves.getValue(LeavesBlock.DISTANCE), 7, "Leaves distance default");
		helper.assertFalse(leaves.getValue(LeavesBlock.PERSISTENT), "Leaves must preserve their persistence default");

		final BlockState thirdPartyDefault = FluidloggedGameTestBootstrap.testBlock.defaultBlockState();
		helper.assertFalse(
				thirdPartyDefault.getValue(LavaloggableBlockSupport.LAVALOGGED),
				"Third-party waterlogged blocks must not contain lava by default"
		);
		helper.assertFalse(
				thirdPartyDefault.getValue(BlockStateProperties.WATERLOGGED),
				"Third-party water default must be preserved"
		);
		helper.assertFalse(
				thirdPartyDefault.getValue(BlockStateProperties.POWERED),
				"Third-party non-fluid defaults must be preserved"
		);

		final BlockState explicitlyLavalogged = thirdPartyDefault.setValue(
				LavaloggableBlockSupport.LAVALOGGED,
				true
		);
		helper.assertTrue(
				explicitlyLavalogged.getValue(LavaloggableBlockSupport.LAVALOGGED),
				"Explicit lavalogged states must remain available"
		);
		helper.succeed();
	}
}
