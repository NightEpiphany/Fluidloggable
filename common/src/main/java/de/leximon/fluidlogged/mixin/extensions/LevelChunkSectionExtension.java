package de.leximon.fluidlogged.mixin.extensions;

import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import net.minecraft.world.level.material.FluidState;

public interface LevelChunkSectionExtension {

    Short2ObjectMap<FluidState> fluidlogged$createAndSetFluidStatesMap();

    Short2ObjectMap<FluidState> fluidlogged$getFluidStates();
    FluidState fluidlogged$setFluidState(int x, int y, int z, FluidState fluidState);

    FluidState fluidlogged$getFluidStateExact(int x, int y, int z);
}
