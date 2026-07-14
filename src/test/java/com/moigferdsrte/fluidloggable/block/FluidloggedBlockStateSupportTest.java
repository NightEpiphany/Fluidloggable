package com.moigferdsrte.fluidloggable.block;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluidloggedBlockStateSupportTest {
	static {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private static final StateDefinition<Block, BlockState> TEST_STATES =
			new StateDefinition.Builder<Block, BlockState>(Blocks.STONE)
					.add(
							WaterloggableBlockSupport.WATERLOGGED,
							LavaloggableBlockSupport.LAVALOGGED,
							BlockStateProperties.POWERED
					)
					.create(Block::defaultBlockState, BlockState::new);

	private static BlockState state(final boolean waterlogged, final boolean lavalogged) {
		return TEST_STATES.any()
				.setValue(WaterloggableBlockSupport.WATERLOGGED, waterlogged)
				.setValue(LavaloggableBlockSupport.LAVALOGGED, lavalogged)
				.setValue(BlockStateProperties.POWERED, false);
	}

	@Test
	void normalizesDoubleTrueToLavaOnly() {
		final BlockState normalized = FluidloggedBlockStateSupport.normalize(state(true, true));
		assertFalse(normalized.getValue(WaterloggableBlockSupport.WATERLOGGED));
		assertTrue(normalized.getValue(LavaloggableBlockSupport.LAVALOGGED));
	}

	@Test
	void effectiveFluidPrefersLavaOverWater() {
		assertSame(
				Fluids.LAVA.getSource(false),
				FluidloggedBlockStateSupport.getFluidState(state(true, true), Fluids.EMPTY.defaultFluidState())
		);
	}

	@Test
	void withFluidSelectsExactlyOneProperty() {
		assertEquals(
				state(true, false),
				FluidloggedBlockStateSupport.withFluid(state(false, false), Fluids.WATER.getSource(false))
		);
		assertEquals(
				state(false, true),
				FluidloggedBlockStateSupport.withFluid(state(true, false), Fluids.LAVA.getSource(false))
		);
		assertEquals(
				state(false, false),
				FluidloggedBlockStateSupport.withFluid(state(true, true), Fluids.EMPTY.defaultFluidState())
		);
		assertEquals(
				state(true, false),
				FluidloggedBlockStateSupport.withFluid(state(false, false), Fluids.FLOWING_WATER.defaultFluidState())
		);
		assertEquals(
				state(false, true),
				FluidloggedBlockStateSupport.withFluid(state(false, false), Fluids.FLOWING_LAVA.defaultFluidState())
		);
	}

	@Test
	void preservesBothPropertiesAndNormalizes() {
		assertEquals(
				state(false, true),
				FluidloggedBlockStateSupport.preserveFluidlogged(state(true, true), state(false, false))
		);
	}

	@Test
	void lavaCanReplaceWaterButWaterCannotReplaceLava() {
		assertTrue(FluidloggedBlockStateSupport.canPlaceFluid(state(true, false), Fluids.LAVA));
		assertFalse(FluidloggedBlockStateSupport.canPlaceFluid(state(false, true), Fluids.WATER));
		assertFalse(FluidloggedBlockStateSupport.canPlaceFluid(state(true, false), Fluids.WATER));
		assertFalse(FluidloggedBlockStateSupport.canPlaceFluid(state(false, true), Fluids.LAVA));
		assertFalse(FluidloggedBlockStateSupport.canPlaceFluid(state(false, false), Fluids.EMPTY));
	}

	@Test
	void recognizesTheEffectiveStoredFluid() {
		assertTrue(FluidloggedBlockStateSupport.containsFluid(state(true, false), Fluids.WATER));
		assertTrue(FluidloggedBlockStateSupport.containsFluid(state(false, true), Fluids.LAVA));
		assertFalse(FluidloggedBlockStateSupport.containsFluid(state(true, true), Fluids.WATER));
		assertTrue(FluidloggedBlockStateSupport.containsFluid(state(true, false), Fluids.FLOWING_WATER));
		assertTrue(FluidloggedBlockStateSupport.canStoreFluid(state(false, false), Fluids.FLOWING_LAVA));
	}

	@Test
	void ignoresOnlyFluidloggedPropertiesWhenComparingStates() {
		assertFalse(FluidloggedBlockStateSupport.hasNonFluidloggedStateChange(
				state(false, true),
				state(false, false)
		));
		assertTrue(FluidloggedBlockStateSupport.hasNonFluidloggedStateChange(
				state(false, true),
				state(false, false).setValue(BlockStateProperties.POWERED, true)
		));
	}

	@Test
	void addsLavaloggedOnlyWhenWaterloggedIsNewAndLavaIsAbsent() {
		assertTrue(FluidloggedBlockStateSupport.shouldAddLavalogged(
				new net.minecraft.world.level.block.state.properties.Property<?>[] {
						WaterloggableBlockSupport.WATERLOGGED
				}
		));
		assertFalse(FluidloggedBlockStateSupport.shouldAddLavalogged(
				new net.minecraft.world.level.block.state.properties.Property<?>[] {
						WaterloggableBlockSupport.WATERLOGGED,
						LavaloggableBlockSupport.LAVALOGGED
				}
		));
		assertFalse(FluidloggedBlockStateSupport.shouldAddLavalogged(
				new net.minecraft.world.level.block.state.properties.Property<?>[] {
						BlockStateProperties.POWERED
				}
		));
	}

	@Test
	void blockDefaultsAlwaysClearLavaloggedOnly() {
		final BlockState generatedDefault = TEST_STATES.any();
		assertTrue(generatedDefault.getValue(LavaloggableBlockSupport.LAVALOGGED));

		final BlockState dryDefault = FluidloggedBlockStateSupport.withDefaultLavaloggedFalse(generatedDefault);

		assertFalse(dryDefault.getValue(LavaloggableBlockSupport.LAVALOGGED));
		assertEquals(
				generatedDefault.getValue(WaterloggableBlockSupport.WATERLOGGED),
				dryDefault.getValue(WaterloggableBlockSupport.WATERLOGGED)
		);
		assertEquals(
				generatedDefault.getValue(BlockStateProperties.POWERED),
				dryDefault.getValue(BlockStateProperties.POWERED)
		);
		assertSame(
				Blocks.STONE.defaultBlockState(),
				FluidloggedBlockStateSupport.withDefaultLavaloggedFalse(Blocks.STONE.defaultBlockState())
		);
	}
}
