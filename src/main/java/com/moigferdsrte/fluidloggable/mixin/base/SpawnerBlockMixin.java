package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpawnerBlock.class)
public abstract class SpawnerBlockMixin extends BaseEntityBlock implements SimpleWaterloggedBlock {
    protected SpawnerBlockMixin(Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(final BlockBehaviour.Properties properties, final CallbackInfo ci) {
        final var state = this.defaultBlockState();
        if (state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            this.registerDefaultState(state.setValue(WaterloggableBlockSupport.WATERLOGGED, false));
        }
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WaterloggableBlockSupport.WATERLOGGED);
    }
}
