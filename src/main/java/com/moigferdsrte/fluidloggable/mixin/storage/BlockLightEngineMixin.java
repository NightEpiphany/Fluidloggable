package com.moigferdsrte.fluidloggable.mixin.storage;

import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.extension.LightEngineExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.LavaFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {
	@Inject(method = "getEmission", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$getStoredLavaEmission(
			final long blockNode,
			final BlockState state,
			final CallbackInfoReturnable<Integer> cir
	) {
		if (state.getLightEmission() > 0) {
			return;
		}

		final BlockPos pos = BlockPos.of(blockNode);
		final LightChunk chunk = ((LightEngineExtension) (Object) this)
				.fluidloggable$getChunkForLighting(pos);
		if (chunk == null) {
			return;
		}

		final var fluidState = chunk.getFluidState(pos);
		if (fluidState.is(FluidTags.LAVA)
				&& fluidState.getType().isSame(Fluids.LAVA)
				&& FluidloggedBlockStateSupport.containsFluid(chunk, pos, state, Fluids.LAVA)) {
			cir.setReturnValue(LavaFluid.LIGHT_EMISSION);
		}
	}
}
