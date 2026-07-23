package com.moigferdsrte.fluidloggable.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class WaterloggableBlockSupport {
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private WaterloggableBlockSupport() {
	}

	public static BlockState withPlacementWater(final BlockState state, final BlockPlaceContext context) {
		return state == null || !state.hasProperty(WATERLOGGED)
				? state
				: state.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(FluidTags.WATER));
	}

	public static BlockState preserveWaterlogged(final BlockState oldState, final BlockState newState) {
		return newState.hasProperty(WATERLOGGED) ? newState.setValue(WATERLOGGED, isWaterlogged(oldState)) : newState;
	}

	public static FluidState getFluidState(final BlockState state, final FluidState fallback) {
		return isWaterlogged(state) ? Fluids.WATER.getSource(false) : fallback;
	}

	public static boolean isWaterlogged(final BlockState state) {
		return state.hasProperty(WATERLOGGED) && state.getValue(WATERLOGGED);
	}

	public static boolean canStoreWater(final BlockState state) {
		return state.hasProperty(WATERLOGGED);
	}

	public static boolean hasNonWaterloggedStateChange(final BlockState oldState, final BlockState newState) {
		if (!oldState.hasProperty(WATERLOGGED) || !newState.hasProperty(WATERLOGGED)) {
			return oldState != newState;
		}
		return newState.setValue(WATERLOGGED, oldState.getValue(WATERLOGGED)) != oldState;
	}

	public static void scheduleWaterTick(final LevelReader level, final ScheduledTickAccess ticks, final BlockPos pos, final BlockState state) {
		if (isWaterlogged(state)) {
			ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
	}

	public static void scheduleWaterTick(final Level level, final BlockPos pos, final BlockState state) {
		if (!level.isClientSide() && isWaterlogged(state)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
	}
}
