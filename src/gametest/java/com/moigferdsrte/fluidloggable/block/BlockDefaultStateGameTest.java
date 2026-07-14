package com.moigferdsrte.fluidloggable.block;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockDefaultStateGameTest implements ModInitializer {
	private static final ResourceKey<Block> THIRD_PARTY_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "third_party_waterlogged")
	);
	private static Block thirdPartyBlock;

	@Override
	public void onInitialize() {
		thirdPartyBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				THIRD_PARTY_BLOCK_KEY,
				new ThirdPartyWaterloggedBlock()
		);
	}

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

		final BlockState thirdPartyDefault = thirdPartyBlock.defaultBlockState();
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

	private static final class ThirdPartyWaterloggedBlock extends Block {
		private ThirdPartyWaterloggedBlock() {
			super(BlockBehaviour.Properties.of().setId(THIRD_PARTY_BLOCK_KEY));
			this.registerDefaultState(this.stateDefinition.any()
					.setValue(BlockStateProperties.WATERLOGGED, false)
					.setValue(BlockStateProperties.POWERED, false));
		}

		@Override
		protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
			builder.add(BlockStateProperties.WATERLOGGED, BlockStateProperties.POWERED);
		}
	}
}
