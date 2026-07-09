package com.moigferdsrte.fluidloggable.compat.farmersdelight.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.function.Supplier;

@Mixin(FeastBlock.class)
public abstract class FeastBlockMixin extends Block implements SimpleWaterloggedBlock {
    public FeastBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/util/function/Supplier;Z)V", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers, CallbackInfo ci) {
        final var state = this.defaultBlockState();
        if (state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            this.registerDefaultState(state.setValue(WaterloggableBlockSupport.WATERLOGGED, false));
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;Ljava/util/function/Supplier;ZZ)V", at = @At("TAIL"))
    private void fluidloggable$defaultToDry2(Properties properties, Supplier<Item> servingItem, boolean hasLeftovers, boolean hasServingParticles, CallbackInfo ci) {
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

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void fluidloggable$scheduleWaterTick(
            BlockState stateIn,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos currentPos,
            Direction facing,
            BlockPos facingPos,
            BlockState facingState,
            RandomSource random,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (WaterloggableBlockSupport.isWaterlogged(stateIn)) {
            WaterloggableBlockSupport.scheduleWaterTick(level, scheduledTickAccess, currentPos, stateIn);
            cir.setReturnValue(
                    WaterloggableBlockSupport.preserveWaterlogged(stateIn, super.updateShape(stateIn, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random))
            );
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
        builder.add(WaterloggableBlockSupport.WATERLOGGED);
    }
}
