package com.moigferdsrte.fluidloggable.compat.create.fly.mixin;

import com.moigferdsrte.fluidloggable.block.LavaloggableBlockSupport;
import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.zurrtum.create.content.redstone.diodes.AbstractDiodeBlock;
import com.zurrtum.create.content.redstone.diodes.ToggleLatchBlock;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ToggleLatchBlock.class)
public abstract class ToggleLatchBlockMixin extends AbstractDiodeBlock implements RedStoneConnectBlock, SimpleWaterloggedBlock {
    public ToggleLatchBlockMixin(Properties builder) {
        super(builder);
    }

    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void fluidloggable$addWaterlogged(final StateDefinition.Builder<Block, BlockState> builder, final CallbackInfo ci) {
        if (!builder.properties.containsKey(WaterloggableBlockSupport.WATERLOGGED.getName())) {
            builder.add(WaterloggableBlockSupport.WATERLOGGED, LavaloggableBlockSupport.LAVALOGGED);
        }
    }
}
