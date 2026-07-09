package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public abstract class BedBlockMixin extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
	protected BedBlockMixin(final BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void fluidloggable$defaultToDry(final DyeColor color, final BlockBehaviour.Properties properties, final CallbackInfo ci) {
		final var state = this.defaultBlockState();
        if (state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            this.registerDefaultState(state.setValue(WaterloggableBlockSupport.WATERLOGGED, false));
        }
	}

	@Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
	private void fluidloggable$waterlogFoot(final BlockPlaceContext context, final CallbackInfoReturnable<@Nullable BlockState> cir) {
		cir.setReturnValue(WaterloggableBlockSupport.withPlacementWater(cir.getReturnValue(), context));
	}

	@Inject(method = "setPlacedBy", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$waterlogHead(
		final Level level,
		final BlockPos pos,
		final BlockState state,
		final @Nullable LivingEntity by,
		final ItemStack itemStack,
		final CallbackInfo ci
	) {
		BlockPos headPos = pos.relative(state.getValue(BedBlock.FACING));
		level.setBlockAndUpdate(
			headPos,
			state.setValue(BedBlock.PART, BedPart.HEAD).setValue(WaterloggableBlockSupport.WATERLOGGED, level.getFluidState(headPos).is(Fluids.WATER))
		);
		ci.cancel();
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
	private void fluidloggable$preserveOwnWaterState(
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

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
		if (fluidloggable$isComfortsBlock()) {
			return;
		}
		builder.add(WaterloggableBlockSupport.WATERLOGGED);
	}

	@Unique
    private boolean fluidloggable$isComfortsBlock() {
		return ((Object) this).getClass().getName().startsWith("com.illusivesoulworks.comforts.common.block.");
	}
}
