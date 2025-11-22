package de.leximon.fluidlogged.mixin.classes.world_interaction.removal_and_placement.extra;

import com.llamalad7.mixinextras.sugar.Local;
import de.leximon.fluidlogged.mixin.extensions.LevelExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @Unique
    private boolean fluidlogged$isWaterlogged = false;

    @Inject(
            method = "moveBlocks",
            at = @At("HEAD")
    )private void captureValue(Level level, BlockPos pos, Direction facing, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        BlockPos blockpos = pos.relative(facing);
        FluidState fluidState = level.getFluidState(blockpos);
        this.fluidlogged$isWaterlogged = fluidState.getType() == Fluids.WATER;
    }


    @Inject(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;size()I",
                    ordinal = 2
            )
    )
    private void injectFluidRemoval(
            Level level, BlockPos blockPos, Direction direction, boolean bl, CallbackInfoReturnable<Boolean> cir,
            @Local Map<BlockPos, BlockState> map
    ) {
        for (BlockPos pos : map.keySet()) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isEmpty())
                continue;
            ((LevelExtension) level).setFluid(pos, Fluids.EMPTY.defaultFluidState(), Block.UPDATE_ALL);
        }
    }

    @Inject(
            method = "moveBlocks",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;updateNeighborsAt(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V",
                    ordinal = 2,
                    shift = At.Shift.AFTER
            )
    )private void injectPistonHead(Level level, BlockPos pos, Direction facing, boolean extending, CallbackInfoReturnable<Boolean> cir) {
        BlockPos blockpos = pos.relative(facing);
        if (this.fluidlogged$isWaterlogged)
            ((LevelExtension) level).setFluid(blockpos, Fluids.WATER.defaultFluidState(), Block.UPDATE_ALL);
    }

}
