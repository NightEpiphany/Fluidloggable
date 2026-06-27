package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractCauldronBlock.class)
public abstract class CauldronBlockMixin extends Block implements SimpleWaterloggedBlock {
    public CauldronBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return WaterloggableBlockSupport.withPlacementWater(super.getStateForPlacement(context), context);
    }

    @Override
    protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
        return WaterloggableBlockSupport.getFluidState(state, super.getFluidState(state));
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
        WaterloggableBlockSupport.scheduleWaterTick(level, ticks, pos, state);
        return WaterloggableBlockSupport.preserveWaterlogged(state, super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random));
    }
}
