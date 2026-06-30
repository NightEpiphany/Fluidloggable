package com.moigferdsrte.fluidloggable.mixin.client;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
	@Redirect(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 0)
	)
	private FluidState fluidloggable$getFluidDown(
		final BlockState state,
		final BlockAndTintGetter level,
		final BlockPos pos,
		final FluidRenderer.Output output,
		final BlockState blockState,
		final FluidState fluidState
	) {
		return fluidloggable$getNeighborFluidState(level, pos, Direction.DOWN);
	}

	@Redirect(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 1)
	)
	private FluidState fluidloggable$getFluidUp(
		final BlockState state,
		final BlockAndTintGetter level,
		final BlockPos pos,
		final FluidRenderer.Output output,
		final BlockState blockState,
		final FluidState fluidState
	) {
		return fluidloggable$getNeighborFluidState(level, pos, Direction.UP);
	}

	@Redirect(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 2)
	)
	private FluidState fluidloggable$getFluidNorth(
		final BlockState state,
		final BlockAndTintGetter level,
		final BlockPos pos,
		final FluidRenderer.Output output,
		final BlockState blockState,
		final FluidState fluidState
	) {
		return fluidloggable$getNeighborFluidState(level, pos, Direction.NORTH);
	}

	@Redirect(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 3)
	)
	private FluidState fluidloggable$getFluidSouth(
		final BlockState state,
		final BlockAndTintGetter level,
		final BlockPos pos,
		final FluidRenderer.Output output,
		final BlockState blockState,
		final FluidState fluidState
	) {
		return fluidloggable$getNeighborFluidState(level, pos, Direction.SOUTH);
	}

	@Redirect(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 4)
	)
	private FluidState fluidloggable$getFluidWest(
		final BlockState state,
		final BlockAndTintGetter level,
		final BlockPos pos,
		final FluidRenderer.Output output,
		final BlockState blockState,
		final FluidState fluidState
	) {
		return fluidloggable$getNeighborFluidState(level, pos, Direction.WEST);
	}

	@Redirect(
		method = "tesselate",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 5)
	)
	private FluidState fluidloggable$getFluidEast(
		final BlockState state,
		final BlockAndTintGetter level,
		final BlockPos pos,
		final FluidRenderer.Output output,
		final BlockState blockState,
		final FluidState fluidState
	) {
		return fluidloggable$getNeighborFluidState(level, pos, Direction.EAST);
	}

	@Redirect(
		method = "getHeight(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)F",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$getHeightUsesStoredFluid(final BlockState state, final BlockAndTintGetter level, final Fluid fluid, final BlockPos pos) {
		return level.getFluidState(pos);
	}

	@Redirect(
		method = "getHeight(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)F",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$getAboveHeightUsesStoredFluid(
		final BlockState state,
		final BlockAndTintGetter level,
		final Fluid fluidType,
		final BlockPos pos,
		final BlockState blockState,
		final FluidState fluidState
	) {
		return level.getFluidState(pos.above());
	}

	private static FluidState fluidloggable$getNeighborFluidState(
		final BlockAndTintGetter level,
		final BlockPos pos,
		final Direction direction
	) {
		final BlockPos neighborPos = pos.relative(direction);
		final BlockState neighborState = level.getBlockState(neighborPos);
		if (WaterloggableBlockSupport.canStoreWater(neighborState) && !WaterloggableBlockSupport.isWaterlogged(neighborState)) {
			return neighborState.getFluidState();
		}
		return level.getFluidState(neighborPos);
	}
}
