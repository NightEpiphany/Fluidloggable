package com.moigferdsrte.fluidloggable.compat.sodium.mixin;

import com.moigferdsrte.fluidloggable.compat.sodium.extension.SodiumClonedChunkSectionExtension;
import com.moigferdsrte.fluidloggable.extension.LevelChunkSectionExtension;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.cloned.ClonedChunkSection")
public class ClonedChunkSectionMixin implements SodiumClonedChunkSectionExtension {
	@Unique
	private @Nullable Short2ObjectMap<FluidState> fluidloggable$fluidStates;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void fluidloggable$copyStoredFluids(
		final Level level,
		final LevelChunk chunk,
		final @Nullable LevelChunkSection section,
		final SectionPos pos,
		final CallbackInfo ci
	) {
		if (section == null) {
			return;
		}

		Short2ObjectMap<FluidState> source = ((LevelChunkSectionExtension)section).fluidloggable$getFluidStates();
		if (source.isEmpty()) {
			return;
		}

		Short2ObjectOpenHashMap<FluidState> copy = new Short2ObjectOpenHashMap<>(source);
		copy.defaultReturnValue(Fluids.EMPTY.defaultFluidState());
		this.fluidloggable$fluidStates = copy;
	}

	@Override
	public @Nullable Short2ObjectMap<FluidState> fluidloggable$getFluidStates() {
		return this.fluidloggable$fluidStates;
	}
}
