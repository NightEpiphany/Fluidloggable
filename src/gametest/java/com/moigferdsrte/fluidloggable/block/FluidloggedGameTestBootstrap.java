package com.moigferdsrte.fluidloggable.block;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
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

import java.util.List;

public final class FluidloggedGameTestBootstrap implements ModInitializer {
	private static final ResourceKey<Block> TEST_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "fluid_channel")
	);
	private static final ResourceKey<Block> TAGGED_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "tagged_channel")
	);
	private static final ResourceKey<Block> LATE_CONFIGURED_BLOCK_KEY = ResourceKey.create(
			Registries.BLOCK,
			Identifier.fromNamespaceAndPath("fluidloggable-gametest", "late_configured_channel")
	);

	public static Block testBlock;
	public static Block taggedBlock;
	public static Block lateConfiguredBlock;

	@Override
	public void onInitialize() {
		testBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				TEST_BLOCK_KEY,
				new FluidChannelBlock()
		);
		lateConfiguredBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				LATE_CONFIGURED_BLOCK_KEY,
				new Block(BlockBehaviour.Properties.of().noCollision().setId(LATE_CONFIGURED_BLOCK_KEY))
		);
		FluidloggableConfig.setFluidloggableBlockIds(List.of(
				TAGGED_BLOCK_KEY.identifier().toString(),
				LATE_CONFIGURED_BLOCK_KEY.identifier().toString()
		));
		taggedBlock = Registry.register(
				BuiltInRegistries.BLOCK,
				TAGGED_BLOCK_KEY,
				new Block(BlockBehaviour.Properties.of().noCollision().setId(TAGGED_BLOCK_KEY))
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
