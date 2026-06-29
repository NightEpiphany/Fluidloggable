package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FenceGateBlock.class)
public abstract class FenceGateBlockMixin extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
	protected FenceGateBlockMixin(final BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fluidloggable$defaultToDry(final WoodType type, final BlockBehaviour.Properties properties, final CallbackInfo ci) {
		final var state = this.defaultBlockState();
        if (state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            this.registerDefaultState(state.setValue(WaterloggableBlockSupport.WATERLOGGED, false));
        }
	}

	@Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$waterlogOnPlacement(final BlockPlaceContext context, final CallbackInfoReturnable<BlockState> cir) {
		cir.setReturnValue(WaterloggableBlockSupport.withPlacementWater(cir.getReturnValue(), context));
	}

	@Override
	protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
		return WaterloggableBlockSupport.getFluidState(state, super.getFluidState(state));
	}

	@Inject(method = "updateShape", at = @At("HEAD"))
	private void fluidloggable$scheduleWaterTick(
		final BlockState state,
		final LevelReader level,
		final ScheduledTickAccess ticks,
		final BlockPos pos,
		final Direction directionToNeighbour,
		final BlockPos neighbourPos,
		final BlockState neighbourState,
		final RandomSource random,
		final CallbackInfoReturnable<BlockState> cir
	) {
		WaterloggableBlockSupport.scheduleWaterTick(level, ticks, pos, state);
	}

	@Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$preserveWaterlogged(
		final BlockState state,
		final LevelReader level,
		final ScheduledTickAccess ticks,
		final BlockPos pos,
		final Direction directionToNeighbour,
		final BlockPos neighbourPos,
		final BlockState neighbourState,
		final RandomSource random,
		final CallbackInfoReturnable<BlockState> cir
	) {
		cir.setReturnValue(WaterloggableBlockSupport.preserveWaterlogged(state, cir.getReturnValue()));
	}

	@Inject(method = "useWithoutItem", at = @At("RETURN"))
	private void fluidloggable$scheduleAfterUse(
		final BlockState state,
		final Level level,
		final BlockPos pos,
		final Player player,
		final BlockHitResult hitResult,
		final CallbackInfoReturnable<InteractionResult> cir
	) {
		if (cir.getReturnValue() == InteractionResult.SUCCESS) {
			WaterloggableBlockSupport.scheduleWaterTick(level, pos, state);
		}
	}

	@Inject(method = "isPathfindable", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$waterPathfindable(final BlockState state, final PathComputationType type, final CallbackInfoReturnable<Boolean> cir) {
		if (type == PathComputationType.WATER) {
			cir.setReturnValue(WaterloggableBlockSupport.isWaterlogged(state));
		}
	}

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
		builder.add(WaterloggableBlockSupport.WATERLOGGED);
	}
}
