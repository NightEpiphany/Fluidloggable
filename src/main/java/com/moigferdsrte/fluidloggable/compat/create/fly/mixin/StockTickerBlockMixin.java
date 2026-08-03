package com.moigferdsrte.fluidloggable.compat.create.fly.mixin;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlock;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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

@Mixin(StockTickerBlock.class)
public abstract class StockTickerBlockMixin extends HorizontalDirectionalBlock implements IBE<StockTickerBlockEntity>, IWrenchable, ItemInventoryProvider<StockTickerBlockEntity>, SimpleWaterloggedBlock {
    protected StockTickerBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> pBuilder, final CallbackInfo ci) {
        if (!pBuilder.properties.containsKey(WaterloggableBlockSupport.WATERLOGGED.getName())) {
            pBuilder.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(final BlockBehaviour.Properties pProperties, final CallbackInfo ci) {
        this.registerDefaultState(FluidloggedBlockStateSupport.defaultToDry(this.defaultBlockState()));
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void fluidloggable$waterlogOnPlacement(final BlockPlaceContext pContext, final CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(FluidloggedBlockStateSupport.withPlacementFluid(cir.getReturnValue(), pContext));
    }

    @Override
    protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
        return FluidloggedBlockStateSupport.getFluidState(state, super.getFluidState(state));
    }

    @Override
    protected @NonNull BlockState updateShape(
            final @NonNull BlockState state,
            final @NonNull LevelReader level,
            final @NonNull ScheduledTickAccess ticks,
            final @NonNull BlockPos pos,
            final @NonNull Direction directionToNeighbour,
            final @NonNull BlockPos neighbourPos,
            final @NonNull BlockState neighbourState,
            final @NonNull RandomSource random
    ) {
        FluidloggedBlockStateSupport.scheduleFluidTick(level, ticks, pos, state);
        return FluidloggedBlockStateSupport.preserveFluidlogged(state, super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random));
    }
}
