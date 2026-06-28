package com.moigferdsrte.fluidloggable.compat.sodium.extension;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

public interface SodiumClonedChunkSectionExtension {
	@Nullable
	Short2ObjectMap<FluidState> fluidloggable$getFluidStates();
}
