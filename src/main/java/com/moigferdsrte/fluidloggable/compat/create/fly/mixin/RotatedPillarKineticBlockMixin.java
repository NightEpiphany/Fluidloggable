package com.moigferdsrte.fluidloggable.compat.create.fly.mixin;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.zurrtum.create.content.kinetics.base.KineticBlock;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RotatedPillarKineticBlock.class, priority = 1001)
public abstract class RotatedPillarKineticBlockMixin extends KineticBlock {
    public RotatedPillarKineticBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull FluidState getFluidState(final @NonNull BlockState state) {
        return FluidloggedBlockStateSupport.getFluidState(state, super.getFluidState(state));
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
        if (!builder.properties.containsKey(WaterloggableBlockSupport.WATERLOGGED.getName())) {
            builder.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
        }
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void fluidloggable$waterlogOnPlacement(final BlockPlaceContext context, final CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(FluidloggedBlockStateSupport.withPlacementFluid(cir.getReturnValue(), context));
    }
}
