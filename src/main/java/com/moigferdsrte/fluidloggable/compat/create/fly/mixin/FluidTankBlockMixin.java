package com.moigferdsrte.fluidloggable.compat.create.fly.mixin;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidTankBlock.class)
public abstract class FluidTankBlockMixin extends Block implements IWrenchable, IBE<FluidTankBlockEntity>, FluidInventoryProvider<FluidTankBlockEntity>, SimpleWaterloggedBlock {
    public FluidTankBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> p_206840_1_, final CallbackInfo ci) {
        if (!p_206840_1_.properties.containsKey(WaterloggableBlockSupport.WATERLOGGED.getName())) {
            p_206840_1_.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(Properties p_i48440_1_, boolean creative, CallbackInfo ci) {
        this.registerDefaultState(FluidloggedBlockStateSupport.defaultToDry(this.defaultBlockState()));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return FluidloggedBlockStateSupport.withPlacementFluid(super.getStateForPlacement(context), context);
    }

    @Override
    protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
        return FluidloggedBlockStateSupport.getFluidState(state, super.getFluidState(state));
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    private void fluidloggable$scheduleWaterTick(
            BlockState pState,
            LevelReader pLevel,
            ScheduledTickAccess tickView,
            BlockPos pCurrentPos,
            Direction pDirection,
            BlockPos pNeighborPos,
            BlockState pNeighborState,
            RandomSource random,
            CallbackInfoReturnable<BlockState> cir
    ) {
        if (WaterloggableBlockSupport.isWaterlogged(pState) || LavaloggableBlockSupport.isLavalogged(pState)) {
            FluidloggedBlockStateSupport.scheduleFluidTick(pLevel, tickView, pCurrentPos, pState);
            cir.setReturnValue(
                    FluidloggedBlockStateSupport.preserveFluidlogged(pState, super.updateShape(pState, pLevel, tickView, pCurrentPos, pDirection, pNeighborPos, pNeighborState, random))
            );
        }
    }
}
