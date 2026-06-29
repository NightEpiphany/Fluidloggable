package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LavaCauldronBlock.class)
public abstract class LavaCauldronBlockMixin extends AbstractCauldronBlock {
    protected LavaCauldronBlockMixin(BlockBehaviour.Properties properties) {
        super(properties, CauldronInteractions.LAVA);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fluidloggable$defaultToDry(BlockBehaviour.Properties properties, CallbackInfo ci) {
        final var state = this.defaultBlockState();
        if (state.hasProperty(WaterloggableBlockSupport.WATERLOGGED)) {
            this.registerDefaultState(state.setValue(WaterloggableBlockSupport.WATERLOGGED, false));
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WaterloggableBlockSupport.WATERLOGGED);
    }
}
