package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LayeredCauldronBlock.class)
public abstract class LayeredCauldronBlockMixin extends AbstractCauldronBlock {
    protected LayeredCauldronBlockMixin(BlockBehaviour.Properties properties, CauldronInteraction.Dispatcher interactions) {
        super(properties, interactions);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(
            Biome.Precipitation precipitationType,
            CauldronInteraction.Dispatcher interactionMap,
            BlockBehaviour.Properties properties,
            CallbackInfo ci
    ) {
        final var state = this.defaultBlockState();
        if (state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            this.registerDefaultState(state.setValue(WaterloggableBlockSupport.WATERLOGGED, false));
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WaterloggableBlockSupport.WATERLOGGED);
    }

    @Inject(method = "receiveStalactiteDrip", at = @At("RETURN"), cancellable = true)
    private void fluidloggable$preserveWaterloggedAfterDrip(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
        BlockState current = level.getBlockState(pos);
        if (WaterloggableBlockSupport.isWaterlogged(state) && current.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            level.setBlockAndUpdate(pos, current.setValue(WaterloggableBlockSupport.WATERLOGGED, true));
        }
    }

    @Inject(method = "lowerFillLevel", at = @At("RETURN"))
    private static void fluidloggable$preserveWaterloggedAfterLowering(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        BlockState current = level.getBlockState(pos);
        if (WaterloggableBlockSupport.isWaterlogged(state) && current.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            level.setBlockAndUpdate(pos, current.setValue(WaterloggableBlockSupport.WATERLOGGED, true));
        }
    }

    @Inject(method = "handlePrecipitation", at = @At("RETURN"))
    private void fluidloggable$preserveWaterloggedAfterPrecipitation(
            BlockState state,
            Level level,
            BlockPos pos,
            Biome.Precipitation precipitation,
            CallbackInfo ci
    ) {
        BlockState current = level.getBlockState(pos);
        if (WaterloggableBlockSupport.isWaterlogged(state) && current.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            level.setBlockAndUpdate(pos, current.setValue(WaterloggableBlockSupport.WATERLOGGED, true));
        }
    }
}
