package com.moigferdsrte.fluidloggable.compat.farmersdelight.mixin;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "vectorwing.farmersdelight.common.block.HangingTomatoBlock")
public abstract class HangingTomatoBlockMixin extends Block {
    protected HangingTomatoBlockMixin(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(final BlockBehaviour.Properties properties, final CallbackInfo ci) {
        final var state = this.defaultBlockState();
        if (state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            this.registerDefaultState(state.setValue(WaterloggableBlockSupport.WATERLOGGED, false));
        }
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
        builder.add(WaterloggableBlockSupport.WATERLOGGED);
    }
}
