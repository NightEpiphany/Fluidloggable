package com.moigferdsrte.fluidloggable.extension;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.world.level.material.FluidState;

public interface LevelChunkSectionExtension {
	Short2ObjectMap<FluidState> fluidloggable$createAndSetFluidStatesMap();

	Short2ObjectMap<FluidState> fluidloggable$copyFluidStates();

	void fluidloggable$copyFluidStatesFrom(LevelChunkSectionExtension source);

	FluidState fluidloggable$setFluidState(int x, int y, int z, FluidState fluidState);

	FluidState fluidloggable$getFluidStateExact(int x, int y, int z);
}
