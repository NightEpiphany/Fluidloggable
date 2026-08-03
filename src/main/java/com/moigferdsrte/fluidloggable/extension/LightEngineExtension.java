package com.moigferdsrte.fluidloggable.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LightChunk;

public interface LightEngineExtension {
	LightChunk fluidloggable$getChunkForLighting(BlockPos pos);
}
