package com.moigferdsrte.fluidloggable.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;

public interface SectionCopyExtension {
	FluidState fluidloggable$getFluidState(BlockPos pos);
}
