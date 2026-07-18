package com.moigferdsrte.fluidloggable.compat.sodium.mixin;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer")
public class DefaultFluidRendererMixin {
	@Redirect(
		method = "isFullBlockFluidSideVisible",
		at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/services/PlatformBlockAccess;shouldOccludeFluid(Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)Z")
	)
	private boolean fluidloggable$shouldOccludeStoredFluid(
		final @Coerce Object blockAccess,
		final net.minecraft.core.Direction adjacentDirection,
		final BlockState adjacentBlockState,
		final FluidState fluid,
		final BlockGetter view,
		final BlockPos selfPos,
		final net.minecraft.core.Direction facing
	) {
		return fluidloggable$getNeighborFluidState(view, selfPos, facing).getType().isSame(fluid.getType());
	}

	@Redirect(
		method = "isFullBlockFluidSideVisible",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$getNeighborFluidState(
		final BlockState otherState,
		final BlockGetter view,
		final BlockPos selfPos,
		final net.minecraft.core.Direction facing,
		final FluidState fluid
	) {
		return fluidloggable$getNeighborFluidState(view, selfPos, facing);
	}

	@Redirect(
		method = "sampleFluidHeight(Lnet/minecraft/client/renderer/block/BlockAndTintGetter;Lnet/minecraft/world/level/material/Fluid;Lnet/minecraft/core/BlockPos;)F",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$getSampleFluidState(
		final BlockState blockState,
		final BlockAndTintGetter world,
		final Fluid fluid,
		final BlockPos blockPos
	) {
		return world.getFluidState(blockPos);
	}

	@Redirect(
		method = "visitExposureNeighbor",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 0)
	)
	private FluidState fluidloggable$getAboveExposureFluidState(
		final BlockState aboveBlockState,
		final BlockAndTintGetter level,
		final BlockPos origin,
		final FluidState fluidState,
		final int xOffset,
		final int zOffset
	) {
		return level.getFluidState(origin.offset(xOffset, 1, zOffset));
	}

	@Redirect(
		method = "visitExposureNeighbor",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;", ordinal = 1)
	)
	private FluidState fluidloggable$getNeighborExposureFluidState(
		final BlockState neighborBlockState,
		final BlockAndTintGetter level,
		final BlockPos origin,
		final FluidState fluidState,
		final int xOffset,
		final int zOffset
	) {
		return level.getFluidState(origin.offset(xOffset, 0, zOffset));
	}

	private static FluidState fluidloggable$getNeighborFluidState(
		final BlockGetter view,
		final BlockPos selfPos,
		final net.minecraft.core.Direction facing
	) {
		final BlockPos neighborPos = selfPos.relative(facing);
		final BlockState neighborState = view.getBlockState(neighborPos);
		return FluidloggedBlockStateSupport.selectFluidForRendering(
				neighborState,
				view.getFluidState(neighborPos),
				neighborState.getFluidState()
		);
	}
}
