package de.leximon.fluidlogged.mixin.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;

public interface ClientLevelExtension {

    void fluidlogged$syncFluidState(BlockPos blockPos, FluidState fluidState);

}
