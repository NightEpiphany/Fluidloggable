package com.moigferdsrte.fluidloggable.block;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class FluidloggedGameTestBootstrap implements ModInitializer {
	private static final ResourceKey<Block> TEST_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "fluid_channel")
	);

	public static Block testBlock;

	@Override
	public void onInitialize() {
		testBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				TEST_BLOCK_KEY,
				new FluidChannelBlock()
		);
	}

	private static final class FluidChannelBlock extends Block {
		private FluidChannelBlock() {
			super(BlockBehaviour.Properties.of().noCollision().setId(TEST_BLOCK_KEY));
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
