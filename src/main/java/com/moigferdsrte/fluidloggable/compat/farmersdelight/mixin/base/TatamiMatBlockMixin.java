package com.moigferdsrte.fluidloggable.compat.farmersdelight.mixin.base;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.TatamiMatBlock;

@Mixin(TatamiMatBlock.class)
public abstract class TatamiMatBlockMixin extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    protected TatamiMatBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(Properties properties, CallbackInfo ci) {
        this.registerDefaultState(FluidloggedBlockStateSupport.defaultToDry(this.defaultBlockState()));
    }

    @Override
    protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
        return FluidloggedBlockStateSupport.getFluidState(state, super.getFluidState(state));
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void fluidloggable$waterlogOnPlacement(final BlockPlaceContext context, final CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(FluidloggedBlockStateSupport.withPlacementFluid(cir.getReturnValue(), context));
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
        if (WaterloggableBlockSupport.isWaterlogged(stateIn) || LavaloggableBlockSupport.isLavalogged(stateIn)) {
            FluidloggedBlockStateSupport.scheduleFluidTick(level, scheduledTickAccess, currentPos, stateIn);
            cir.setReturnValue(
                    FluidloggedBlockStateSupport.preserveFluidlogged(stateIn, super.updateShape(stateIn, level, scheduledTickAccess, currentPos, facing, facingPos, facingState, random))
            );
        }
    }

    @Redirect(
            method = "playerWillDestroy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    private boolean fluidloggable$preserveExactFluidInOtherHalf(
            final Level level,
            final BlockPos pos,
            final BlockState blockState,
            final int updateFlags
    ) {
        final FluidState storedFluid = level.getFluidState(pos);
        final BlockState replacement = storedFluid.isEmpty()
                ? Blocks.AIR.defaultBlockState()
                : storedFluid.createLegacyBlock();
        return level.setBlock(pos, replacement, updateFlags);
    }

    @Redirect(
            method = "setPlacedBy",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
            )
    )
    private boolean fluidloggable$preserveExactFluidWhenPlacingOtherHalf(
            final Level level,
            final BlockPos otherPos,
            final BlockState requestedState,
            final int updateFlags
    ) {
        final FluidState storedFluid = level.getFluidState(otherPos);
        final BlockState placementState = FluidloggedBlockStateSupport.withFluid(requestedState, storedFluid);
        return ((LevelExtension)level).fluidloggable$setBlockAndInsertFluidIfPossible(
                otherPos,
                placementState,
                updateFlags
        );
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
        builder.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
    }
}
