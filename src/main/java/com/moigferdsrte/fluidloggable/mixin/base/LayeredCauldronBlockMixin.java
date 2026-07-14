package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
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
import org.spongepowered.asm.mixin.Unique;
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
        this.registerDefaultState(FluidloggedBlockStateSupport.defaultToDry(this.defaultBlockState()));
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
    }

    @Inject(method = "receiveStalactiteDrip", at = @At("RETURN"), cancellable = true)
    private void fluidloggable$preserveWaterloggedAfterDrip(BlockState state, Level level, BlockPos pos, Fluid fluid, CallbackInfo ci) {
		fluidloggable$preserveFluidlogged(state, level, pos);
    }

    @Inject(method = "lowerFillLevel", at = @At("RETURN"))
    private static void fluidloggable$preserveWaterloggedAfterLowering(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
		fluidloggable$preserveFluidlogged(state, level, pos);
    }

    @Inject(method = "handlePrecipitation", at = @At("RETURN"))
    private void fluidloggable$preserveWaterloggedAfterPrecipitation(
            BlockState state,
            Level level,
            BlockPos pos,
            Biome.Precipitation precipitation,
            CallbackInfo ci
    ) {
		fluidloggable$preserveFluidlogged(state, level, pos);
    }

	@Unique
	private static void fluidloggable$preserveFluidlogged(final BlockState state, final Level level, final BlockPos pos) {
		final BlockState current = level.getBlockState(pos);
		final BlockState preserved = FluidloggedBlockStateSupport.preserveFluidlogged(state, current);
		if (preserved != current) {
			level.setBlockAndUpdate(pos, preserved);
		}
	}
}
