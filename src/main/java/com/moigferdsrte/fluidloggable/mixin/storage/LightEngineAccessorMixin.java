package com.moigferdsrte.fluidloggable.mixin.storage;

import com.moigferdsrte.fluidloggable.extension.LightEngineExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LightEngine.class)
public abstract class LightEngineAccessorMixin implements LightEngineExtension {
	@Shadow
	protected abstract LightChunk getChunk(int chunkX, int chunkZ);

	@Override
	public LightChunk fluidloggable$getChunkForLighting(final BlockPos pos) {
		return this.getChunk(
				SectionPos.blockToSectionCoord(pos.getX()),
				SectionPos.blockToSectionCoord(pos.getZ())
		);
	}
}
