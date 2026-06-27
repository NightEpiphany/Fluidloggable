package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TripWireBlock.class)
public abstract class TripwireBlockMixin extends Block implements SimpleWaterloggedBlock {
    public TripwireBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(Block hook, Properties properties, CallbackInfo ci) {
        this.registerDefaultState(this.defaultBlockState().setValue(WaterloggableBlockSupport.WATERLOGGED, false));
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void fluidloggable$waterlogOnPlacement(final BlockPlaceContext context, final CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(WaterloggableBlockSupport.withPlacementWater(cir.getReturnValue(), context));
    }

    @Override
    protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
        return WaterloggableBlockSupport.getFluidState(state, super.getFluidState(state));
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void fluidloggable$scheduleWaterTick(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction directionToNeighbour,
            BlockPos neighbourPos,
            BlockState neighbourState,
            RandomSource random,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (state.getValue(WaterloggableBlockSupport.WATERLOGGED)) {
            WaterloggableBlockSupport.scheduleWaterTick(level, ticks, pos, state);
            cir.setReturnValue(
                    WaterloggableBlockSupport.preserveWaterlogged(state, super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random))
            );
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
        builder.add(WaterloggableBlockSupport.WATERLOGGED);
    }
}
