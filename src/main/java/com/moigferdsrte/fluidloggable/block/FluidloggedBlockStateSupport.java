package com.moigferdsrte.fluidloggable.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidloggedBlockStateSupport {
	private FluidloggedBlockStateSupport() {
	}

	public static BlockState normalize(final BlockState state) {
		if (LavaloggableBlockSupport.isLavalogged(state)
				&& WaterloggableBlockSupport.isWaterlogged(state)) {
			return state.setValue(WaterloggableBlockSupport.WATERLOGGED, false);
		}
		return state;
	}

	public static BlockState defaultToDry(final BlockState state) {
		return withFluid(state, Fluids.EMPTY.defaultFluidState());
	}

	public static BlockState withDefaultLavaloggedFalse(final BlockState state) {
		return LavaloggableBlockSupport.isLavalogged(state)
				? state.setValue(LavaloggableBlockSupport.LAVALOGGED, false)
				: state;
	}

	public static BlockState withDefaultFluidPropertiesFalse(final BlockState state) {
		return ConfiguredFluidloggableBlockSupport.isConfigured(state)
				? defaultToDry(state)
				: withDefaultLavaloggedFalse(state);
	}

	public static BlockState withPlacementFluid(final BlockState state, final BlockPlaceContext context) {
		return state == null ? null : withFluid(state, context.getLevel().getFluidState(context.getClickedPos()));
	}

	public static BlockState withFluid(final BlockState state, final FluidState fluidState) {
		BlockState result = state;
		if (result.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
			result = result.setValue(WaterloggableBlockSupport.WATERLOGGED, false);
		}
		if (result.hasProperty(LavaloggableBlockSupport.LAVALOGGED)) {
			result = result.setValue(LavaloggableBlockSupport.LAVALOGGED, false);
		}
		if (isLava(fluidState.getType()) && result.hasProperty(LavaloggableBlockSupport.LAVALOGGED)) {
			return result.setValue(LavaloggableBlockSupport.LAVALOGGED, true);
		}
		if (isWater(fluidState.getType()) && result.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
			return result.setValue(WaterloggableBlockSupport.WATERLOGGED, true);
		}
		return result;
	}

	public static BlockState preserveFluidlogged(final BlockState oldState, final BlockState newState) {
		BlockState result = newState;
		if (result.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
			result = result.setValue(
					WaterloggableBlockSupport.WATERLOGGED,
					WaterloggableBlockSupport.isWaterlogged(oldState)
			);
		}
		if (result.hasProperty(LavaloggableBlockSupport.LAVALOGGED)) {
			result = result.setValue(
					LavaloggableBlockSupport.LAVALOGGED,
					LavaloggableBlockSupport.isLavalogged(oldState)
			);
		}
		return normalize(result);
	}

	public static FluidState getFluidState(final BlockState state, final FluidState fallback) {
		if (LavaloggableBlockSupport.isLavalogged(state)) {
			return Fluids.LAVA.getSource(false);
		}
		return WaterloggableBlockSupport.isWaterlogged(state) ? Fluids.WATER.getSource(false) : fallback;
	}

	public static boolean canStoreFluid(final BlockState state, final Fluid fluid) {
		if (!isSupportedFluid(fluid)) {
			return false;
		}
		if (hasNativeFluidContainer(state)) {
			return isLava(fluid)
					? LavaloggableBlockSupport.canStoreLava(state)
					: WaterloggableBlockSupport.canStoreWater(state);
		}
		return ConfiguredFluidloggableBlockSupport.isConfigured(state);
	}

	public static boolean canStoreFluid(
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state,
			final Fluid fluid
	) {
		if (ConfiguredFluidloggableBlockSupport.isConfigured(state)) {
			return isSupportedFluid(fluid)
					&& ConfiguredFluidloggableBlockSupport.isConfigured(state, level, pos);
		}
		if (hasNativeFluidContainer(state)) {
			return canStoreFluid(state, fluid);
		}
		return isSupportedFluid(fluid)
				&& ConfiguredFluidloggableBlockSupport.isConfigured(state, level, pos);
	}

	public static boolean isSupportedFluid(final Fluid fluid) {
		return isWater(fluid) || isLava(fluid);
	}

	public static FluidState selectFluidForRendering(
			final BlockState state,
			final FluidState storedFluid,
			final FluidState fallback
	) {
		return !storedFluid.isEmpty() && containsFluid(state, storedFluid.getType())
				? storedFluid
				: fallback;
	}

	public static FluidState selectFluidForRendering(
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state,
			final FluidState storedFluid,
			final FluidState fallback
	) {
		return !storedFluid.isEmpty() && containsFluid(level, pos, state, storedFluid.getType())
				? storedFluid
				: fallback;
	}

	public static boolean hasDifferentLightEmission(final FluidState previous, final FluidState current) {
		return isLava(previous.getType()) != isLava(current.getType());
	}

	public static boolean containsFluid(final BlockState state, final Fluid fluid) {
		if (hasNativeFluidContainer(state)) {
			if (isLava(fluid)) {
				return LavaloggableBlockSupport.isLavalogged(state);
			}
			return isWater(fluid)
					&& !LavaloggableBlockSupport.isLavalogged(state)
					&& WaterloggableBlockSupport.isWaterlogged(state);
		}
		return isSupportedFluid(fluid) && ConfiguredFluidloggableBlockSupport.isConfigured(state);
	}

	public static boolean containsFluid(
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state,
			final Fluid fluid
	) {
		if (ConfiguredFluidloggableBlockSupport.isConfigured(state)
				&& !ConfiguredFluidloggableBlockSupport.isConfigured(state, level, pos)) {
			return false;
		}
		if (hasNativeFluidContainer(state)) {
			return containsFluid(state, fluid);
		}
		return isSupportedFluid(fluid)
				&& ConfiguredFluidloggableBlockSupport.isConfigured(state, level, pos);
	}

	public static boolean hasNonFluidloggedStateChange(final BlockState oldState, final BlockState newState) {
		return preserveFluidlogged(oldState, newState) != oldState;
	}

	public static boolean shouldAddLavalogged(final Property<?>[] properties) {
		boolean addsWaterlogged = false;
		for (Property<?> property : properties) {
			if (property == LavaloggableBlockSupport.LAVALOGGED) {
				return false;
			}
			addsWaterlogged |= property == WaterloggableBlockSupport.WATERLOGGED;
		}
		return addsWaterlogged;
	}

	public static boolean canPlaceFluid(final BlockState state, final Fluid fluid) {
		if (hasNativeFluidContainer(state)) {
			if (isLava(fluid)) {
				return LavaloggableBlockSupport.canStoreLava(state)
						&& !LavaloggableBlockSupport.isLavalogged(state);
			}
			return isWater(fluid)
					&& WaterloggableBlockSupport.canStoreWater(state)
					&& !WaterloggableBlockSupport.isWaterlogged(state)
					&& !LavaloggableBlockSupport.isLavalogged(state);
		}
		return isSupportedFluid(fluid) && ConfiguredFluidloggableBlockSupport.isConfigured(state);
	}

	public static boolean canPlaceFluid(
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state,
			final Fluid fluid
	) {
		if (!level.getFluidState(pos).isEmpty()) {
			return false;
		}
		if (ConfiguredFluidloggableBlockSupport.isConfigured(state)) {
			return isSupportedFluid(fluid)
					&& ConfiguredFluidloggableBlockSupport.isConfigured(state, level, pos);
		}
		if (hasNativeFluidContainer(state)) {
			return canPlaceFluid(state, fluid);
		}
		return isSupportedFluid(fluid)
				&& ConfiguredFluidloggableBlockSupport.isConfigured(state, level, pos);
	}

	public static void scheduleFluidTick(
			final LevelReader level,
			final ScheduledTickAccess ticks,
			final BlockPos pos,
			final BlockState state
	) {
		if (LavaloggableBlockSupport.isLavalogged(state)) {
			LavaloggableBlockSupport.scheduleLavaTick(level, ticks, pos, state);
		} else {
			WaterloggableBlockSupport.scheduleWaterTick(level, ticks, pos, state);
		}
	}

	public static void scheduleFluidTick(final Level level, final BlockPos pos, final BlockState state) {
		if (LavaloggableBlockSupport.isLavalogged(state)) {
			LavaloggableBlockSupport.scheduleLavaTick(level, pos, state);
		} else {
			WaterloggableBlockSupport.scheduleWaterTick(level, pos, state);
		}
	}

	private static boolean isWater(final Fluid fluid) {
		return fluid.isSame(Fluids.WATER);
	}

	private static boolean hasNativeFluidContainer(final BlockState state) {
		return state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)
				|| state.hasProperty(LavaloggableBlockSupport.LAVALOGGED);
	}

	private static boolean isLava(final Fluid fluid) {
		return fluid.isSame(Fluids.LAVA);
	}
}
