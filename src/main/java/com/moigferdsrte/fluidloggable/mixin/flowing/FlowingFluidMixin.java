package com.moigferdsrte.fluidloggable.mixin.flowing;

import com.llamalad7.mixinextras.sugar.Local;
import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FlowingFluid.class)
public abstract class FlowingFluidMixin {
	@Unique
	private BlockPos fluidloggable$lastCheckedFluidPos;

	@Shadow
	private static boolean canPassThroughWall(
		final Direction direction,
		final BlockGetter level,
		final BlockPos sourcePos,
		final BlockState sourceState,
		final BlockPos targetPos,
		final BlockState targetState
	) {
		throw new AssertionError();
	}

	@Inject(method = "canMaybePassThrough", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$letWaterPassThroughWaterloggableBlocks(
		final BlockGetter level,
		final BlockPos sourcePos,
		final BlockState sourceState,
		final Direction direction,
		final BlockPos testPos,
		final BlockState testState,
		final FluidState testFluidState,
		final CallbackInfoReturnable<Boolean> cir
	) {
		if (this.fluidloggable$isWaterPermeableTarget(level, sourcePos, sourceState, direction, testPos, testState, testFluidState)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "canHoldSpecificFluid", at = @At("HEAD"), cancellable = true)
	private static void fluidloggable$letWaterloggableBlocksHoldPassingWater(
		final BlockGetter level,
		final BlockPos pos,
		final BlockState state,
		final Fluid newFluid,
		final CallbackInfoReturnable<Boolean> cir
	) {
		if (WaterloggableBlockSupport.canStoreWater(state) && fluidloggable$isSameWater(newFluid)) {
			FluidState currentFluid = level.getFluidState(pos);
			cir.setReturnValue(currentFluid.isEmpty() || currentFluid.getType().isSame(newFluid));
		}
	}

	@Inject(method = "spreadTo", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$spreadIntoWaterloggableBlock(
		final LevelAccessor level,
		final BlockPos pos,
		final BlockState state,
		final Direction direction,
		final FluidState target,
		final CallbackInfo ci
	) {
		if (WaterloggableBlockSupport.canStoreWater(state) && fluidloggable$isSameWater(target.getType())) {
			((LevelExtension)level).fluidloggable$setFluid(pos, target, Block.UPDATE_ALL | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK);
			ci.cancel();
		}
	}

	@Redirect(
		method = "tick",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z")
	)
	private boolean fluidloggable$tickStoredFluidInsteadOfReplacingBlock(
		final ServerLevel level,
		final BlockPos pos,
		final BlockState newState,
		final int flags,
		final ServerLevel originalLevel,
		final BlockPos originalPos,
		final BlockState originalBlockState,
		final FluidState currentFluidState
	) {
		BlockState previousBlock = level.getBlockState(pos);
		if (WaterloggableBlockSupport.canStoreWater(previousBlock) && fluidloggable$isSameWater(currentFluidState.getType())) {
			((LevelExtension)level).fluidloggable$setFluid(pos, newState.getFluidState(), flags);
			return false;
		}

		((LevelExtension)level).fluidloggable$setFluid(pos, Fluids.EMPTY.defaultFluidState(), flags);
		return level.setBlock(pos, newState, flags);
	}

	@Redirect(
		method = "getNewLiquid",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
	)
	private BlockState fluidloggable$captureGetNewLiquidPos(final ServerLevel level, final BlockPos pos) {
		this.fluidloggable$lastCheckedFluidPos = pos.immutable();
		return level.getBlockState(pos);
	}

	@Redirect(
		method = "getNewLiquid",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$getNewLiquidUsesStoredFluid(
		final BlockState state,
		final ServerLevel level,
		final BlockPos pos,
		final BlockState blockState
	) {
		return level.getFluidState(this.fluidloggable$lastCheckedFluidPos);
	}

	@Redirect(
		method = "spread",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$spreadUsesStoredFluid(
		final BlockState belowState,
		final ServerLevel level,
		final BlockPos pos,
		final BlockState state,
		final FluidState fluidState
	) {
		return level.getFluidState(pos.below());
	}

	@Redirect(
		method = "getSlopeDistance",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$getSlopeDistanceUsesStoredFluid(
		final BlockState testState,
		final LevelReader level,
		final BlockPos pos,
		final int pass,
		final Direction from,
		final BlockState state,
		@Coerce final Object context,
		@Local(name = "testPos") final BlockPos testPos
	) {
		return level.getFluidState(testPos);
	}

	@Redirect(
		method = "getSpread",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
	)
	private BlockState fluidloggable$captureGetSpreadPos(final ServerLevel level, final BlockPos pos) {
		this.fluidloggable$lastCheckedFluidPos = pos.immutable();
		return level.getBlockState(pos);
	}

	@Redirect(
		method = "getSpread",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$getSpreadUsesStoredFluid(
		final BlockState state,
		final ServerLevel level,
		final BlockPos pos,
		final BlockState blockState
	) {
		return level.getFluidState(this.fluidloggable$lastCheckedFluidPos);
	}

	@Redirect(
		method = "isWaterHole",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;")
	)
	private FluidState fluidloggable$isWaterHoleUsesStoredFluid(
		final BlockState state,
		final BlockGetter level,
		final BlockPos topPos,
		final BlockState topState,
		final BlockPos bottomPos,
		final BlockState bottomState
	) {
		return level.getFluidState(bottomPos);
	}

	@Unique
	private boolean fluidloggable$isWaterPermeableTarget(
		final BlockGetter level,
		final BlockPos sourcePos,
		final BlockState sourceState,
		final Direction direction,
		final BlockPos targetPos,
		final BlockState targetState,
		final FluidState targetFluidState
	) {
		return WaterloggableBlockSupport.canStoreWater(targetState)
			&& targetFluidState.getType().isSame((Fluid)(Object)this)
			&& fluidloggable$isSameWater(targetFluidState.getType())
			&& canPassThroughWall(direction, level, sourcePos, sourceState, targetPos, targetState);
	}

	@Unique
	private static boolean fluidloggable$isSameWater(final Fluid fluid) {
		return fluid.isSame(Fluids.WATER);
	}
}
