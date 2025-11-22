package de.leximon.fluidlogged.mixin.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;

public interface RenderChunkExtension {

    FluidState fluidlogged$getFluidState(BlockPos blockPos);

}
