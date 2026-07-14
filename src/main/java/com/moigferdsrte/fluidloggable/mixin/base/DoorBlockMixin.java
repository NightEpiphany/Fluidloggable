package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DoorBlock.class)
public abstract class DoorBlockMixin extends Block implements SimpleWaterloggedBlock {
	protected DoorBlockMixin(final BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fluidloggable$defaultToDry(final BlockSetType type, final BlockBehaviour.Properties properties, final CallbackInfo ci) {
		this.registerDefaultState(FluidloggedBlockStateSupport.defaultToDry(this.defaultBlockState()));
	}

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void fluidloggable$addWaterloggedProperty(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
		builder.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
	}

	@Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$waterlogLowerHalf(final BlockPlaceContext context, final CallbackInfoReturnable<@Nullable BlockState> cir) {
		cir.setReturnValue(FluidloggedBlockStateSupport.withPlacementFluid(cir.getReturnValue(), context));
	}

	@Inject(method = "setPlacedBy", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$waterlogUpperHalf(
		final Level level,
		final BlockPos pos,
		final BlockState state,
		final @Nullable LivingEntity by,
		final ItemStack itemStack,
		final CallbackInfo ci
	) {
		BlockPos upperPos = pos.above();
		((LevelExtension) level).fluidloggable$setBlockAndInsertFluidIfPossible(
				upperPos,
				FluidloggedBlockStateSupport.withFluid(
				state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
				level.getFluidState(upperPos)
				),
				Block.UPDATE_ALL
		);
		ci.cancel();
	}

	@Override
	protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
		return FluidloggedBlockStateSupport.getFluidState(state, super.getFluidState(state));
	}

	@Inject(method = "updateShape", at = @At("HEAD"))
	private void fluidloggable$scheduleWaterTickOnShapeUpdate(
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
		FluidloggedBlockStateSupport.scheduleFluidTick(level, ticks, pos, state);
	}

	@Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$preserveHalfWaterState(
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
		cir.setReturnValue(FluidloggedBlockStateSupport.preserveFluidlogged(state, cir.getReturnValue()));
	}

	@Inject(method = "isPathfindable", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$waterPathfindableWhenWaterlogged(
		final BlockState state,
		final PathComputationType type,
		final CallbackInfoReturnable<Boolean> cir
	) {
		if (type == PathComputationType.WATER) {
			cir.setReturnValue(WaterloggableBlockSupport.isWaterlogged(state));
		}
	}

	@Inject(method = "useWithoutItem", at = @At("RETURN"))
	private void fluidloggable$scheduleWaterTickAfterUse(
		final BlockState state,
		final Level level,
		final BlockPos pos,
		final net.minecraft.world.entity.player.Player player,
		final net.minecraft.world.phys.BlockHitResult hitResult,
		final CallbackInfoReturnable<InteractionResult> cir
	) {
		if (cir.getReturnValue() == InteractionResult.SUCCESS) {
			final BlockState currentState = level.getBlockState(pos);
			FluidloggedBlockStateSupport.scheduleFluidTick(level, pos, currentState);
		}
	}

	@Inject(method = "setOpen", at = @At("RETURN"))
	private void fluidloggable$scheduleWaterTickAfterSetOpen(
		final @Nullable Entity sourceEntity,
		final Level level,
		final BlockState state,
		final BlockPos pos,
		final boolean shouldOpen,
		final CallbackInfo ci
	) {
		FluidloggedBlockStateSupport.scheduleFluidTick(level, pos, level.getBlockState(pos));
	}

	@Inject(method = "neighborChanged", at = @At("RETURN"))
	private void fluidloggable$scheduleWaterTickAfterRedstoneUpdate(
		final BlockState state,
		final Level level,
		final BlockPos pos,
		final Block block,
		final @Nullable Orientation orientation,
		final boolean movedByPiston,
		final CallbackInfo ci
	) {
		FluidloggedBlockStateSupport.scheduleFluidTick(level, pos, level.getBlockState(pos));
	}
}
