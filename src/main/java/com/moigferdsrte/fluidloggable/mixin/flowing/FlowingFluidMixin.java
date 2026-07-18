package com.moigferdsrte.fluidloggable.mixin.flowing;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import com.moigferdsrte.fluidloggable.flowing.FluidFlowBarrier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
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

	@Shadow
	private static boolean canHoldAnyFluid(final BlockState state) {
		throw new AssertionError();
	}

	@Shadow
	protected abstract FluidState getNewLiquid(ServerLevel level, BlockPos pos, BlockState state);

	@Shadow
	protected abstract int getSpreadDelay(Level level, BlockPos pos, FluidState oldFluidState, FluidState newFluidState);

	@Shadow
	protected abstract void spread(ServerLevel level, BlockPos pos, BlockState state, FluidState fluidState);

	@Inject(method = "canMaybePassThrough", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$letStoredFluidPassThroughFluidloggedBlocks(
		final BlockGetter level,
		final BlockPos sourcePos,
		final BlockState sourceState,
		final Direction direction,
		final BlockPos testPos,
		final BlockState testState,
		final FluidState testFluidState,
		final CallbackInfoReturnable<Boolean> cir
	) {
		final Fluid runningFluid = (Fluid) (Object) this;
		if (!FluidloggedBlockStateSupport.isSupportedFluid(runningFluid)) {
			return;
		}

		final boolean sourceContainsRunningFluid = fluidloggable$containsStoredFluid(level, sourcePos, sourceState, runningFluid);
		final boolean targetCanStoreRunningFluid = FluidloggedBlockStateSupport.canStoreFluid(testState, runningFluid);
		final FluidState exactTargetFluidState = level.getFluidState(testPos);
		final boolean targetContainsStoredFluid = fluidloggable$containsStoredFluid(
				level,
				testPos,
				testState,
				exactTargetFluidState.getType()
		);
		if (!sourceContainsRunningFluid && !targetCanStoreRunningFluid && !targetContainsStoredFluid) {
			return;
		}

		final boolean targetIsRunningSource = exactTargetFluidState.isSource()
				&& exactTargetFluidState.getType().isSame(runningFluid);
		cir.setReturnValue(
				!targetIsRunningSource
						&& (targetCanStoreRunningFluid || targetContainsStoredFluid || canHoldAnyFluid(testState))
						&& this.fluidloggable$canPassThroughStoredFluidWall(
								direction,
								level,
								sourcePos,
								sourceState,
								testPos,
								testState
						)
		);
	}

	@Inject(method = "canHoldSpecificFluid", at = @At("HEAD"), cancellable = true)
	private static void fluidloggable$letFluidloggedBlocksHoldPassingFluid(
		final BlockGetter level,
		final BlockPos pos,
		final BlockState state,
		final Fluid newFluid,
		final CallbackInfoReturnable<Boolean> cir
	) {
		if (FluidloggedBlockStateSupport.isSupportedFluid(newFluid)
				&& FluidloggedBlockStateSupport.canStoreFluid(state, newFluid)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "spreadTo", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$spreadIntoFluidloggedBlock(
		final LevelAccessor level,
		final BlockPos pos,
		final BlockState state,
		final Direction direction,
		final FluidState target,
		final CallbackInfo ci
	) {
		final Fluid targetFluid = target.getType();
		final FluidState currentFluid = level.getFluidState(pos);
		if (FluidloggedBlockStateSupport.isSupportedFluid(targetFluid)
				&& FluidloggedBlockStateSupport.canStoreFluid(state, targetFluid)) {
			if (currentFluid.isEmpty() || currentFluid.getType().isSame(targetFluid)) {
				((LevelExtension)level).fluidloggable$setFluid(
						pos,
						target,
						Block.UPDATE_ALL | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK
				);
			}
			ci.cancel();
		}
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$tickStoredFluidWithoutReplacingBlock(
		final ServerLevel level,
		final BlockPos pos,
		final BlockState blockState,
		final FluidState fluidState,
		final CallbackInfo ci
	) {
		BlockState currentBlockState = level.getBlockState(pos);
		if (!FluidloggedBlockStateSupport.isSupportedFluid(fluidState.getType())
				|| !FluidloggedBlockStateSupport.canStoreFluid(currentBlockState, fluidState.getType())) {
			return;
		}

		FluidState currentFluidState = fluidState;
		if (!fluidState.isSource()) {
			FluidState newFluidState = this.getNewLiquid(level, pos, currentBlockState);
			int tickDelay = this.getSpreadDelay(level, pos, fluidState, newFluidState);
			if (newFluidState.isEmpty()) {
				((LevelExtension)level).fluidloggable$setFluid(pos, Fluids.EMPTY.defaultFluidState(), Block.UPDATE_ALL);
				ci.cancel();
				return;
			}

			if (newFluidState != fluidState) {
				currentFluidState = newFluidState;
				((LevelExtension)level).fluidloggable$setFluid(pos, newFluidState, Block.UPDATE_ALL);
				level.scheduleTick(pos, newFluidState.getType(), tickDelay);
			}
		}

		this.spread(level, pos, currentBlockState, currentFluidState);
		ci.cancel();
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
		if (FluidloggedBlockStateSupport.isSupportedFluid(currentFluidState.getType())
				&& FluidloggedBlockStateSupport.canStoreFluid(previousBlock, currentFluidState.getType())) {
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
		method = "getNewLiquid",
		at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/material/FlowingFluid;canPassThroughWall(Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
		)
	)
	private boolean fluidloggable$getNewLiquidIgnoresContainingBlockCollision(
			final Direction direction,
			final BlockGetter level,
			final BlockPos sourcePos,
			final BlockState sourceState,
			final BlockPos targetPos,
			final BlockState targetState
	) {
		return this.fluidloggable$canPassThroughStoredFluidWall(
				direction,
				level,
				sourcePos,
				sourceState,
				targetPos,
				targetState
		);
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

	@Redirect(
		method = "isWaterHole",
		at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/world/level/material/FlowingFluid;canPassThroughWall(Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"
		)
	)
	private boolean fluidloggable$isWaterHoleIgnoresContainingBlockCollision(
			final Direction direction,
			final BlockGetter level,
			final BlockPos sourcePos,
			final BlockState sourceState,
			final BlockPos targetPos,
			final BlockState targetState
	) {
		return this.fluidloggable$canPassThroughStoredFluidWall(
				direction,
				level,
				sourcePos,
				sourceState,
				targetPos,
				targetState
		);
	}

	@Unique
	private boolean fluidloggable$canPassThroughStoredFluidWall(
			final Direction direction,
			final BlockGetter level,
			final BlockPos sourcePos,
			final BlockState sourceState,
			final BlockPos targetPos,
			final BlockState targetState
	) {
		if (FluidFlowBarrier.blocksPassage(direction, sourceState, targetState)) {
			return false;
		}

		final Fluid runningFluid = (Fluid)(Object)this;
		final BlockState effectiveSourceState = fluidloggable$isPassableFluidContainer(
				level,
				sourcePos,
				sourceState,
				runningFluid
		)
				? Blocks.AIR.defaultBlockState()
				: sourceState;
		final BlockState effectiveTargetState = fluidloggable$isPassableFluidContainer(
				level,
				targetPos,
				targetState,
				runningFluid
		)
				? Blocks.AIR.defaultBlockState()
				: targetState;
		return canPassThroughWall(
				direction,
				level,
				sourcePos,
				effectiveSourceState,
				targetPos,
				effectiveTargetState
		);
	}

	@Unique
	private static boolean fluidloggable$isPassableFluidContainer(
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state,
			final Fluid runningFluid
	) {
		return fluidloggable$containsAnyStoredFluid(level, pos, state)
				|| FluidloggedBlockStateSupport.canStoreFluid(state, runningFluid);
	}

	@Unique
	private static boolean fluidloggable$containsAnyStoredFluid(
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state
	) {
		final FluidState storedFluid = level.getFluidState(pos);
		return !storedFluid.isEmpty()
				&& FluidloggedBlockStateSupport.isSupportedFluid(storedFluid.getType())
				&& FluidloggedBlockStateSupport.containsFluid(state, storedFluid.getType());
	}

	@Unique
	private static boolean fluidloggable$containsStoredFluid(
			final BlockGetter level,
			final BlockPos pos,
			final BlockState state,
			final Fluid fluid
	) {
		return FluidloggedBlockStateSupport.isSupportedFluid(fluid)
				&& FluidloggedBlockStateSupport.containsFluid(state, fluid)
				&& level.getFluidState(pos).getType().isSame(fluid);
	}

}
