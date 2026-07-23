package com.moigferdsrte.fluidloggable.mixin.client;

import com.moigferdsrte.fluidloggable.extension.LevelChunkSectionExtension;
import com.moigferdsrte.fluidloggable.extension.SectionCopyExtension;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.client.renderer.chunk.SectionCopy;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(SectionCopy.class)
public abstract class SectionCopyMixin implements SectionCopyExtension {
	@Unique
	private Short2ObjectMap<FluidState> fluidloggable$fluidStates;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void fluidloggable$copyStoredFluids(final LevelChunk levelChunk, final int sectionIndex, final CallbackInfo ci) {
		if (levelChunk instanceof EmptyLevelChunk) {
			return;
		}

		LevelChunkSection[] sections = levelChunk.getSections();
		if (sectionIndex < 0 || sectionIndex >= sections.length) {
			return;
		}

		Short2ObjectMap<FluidState> source = ((LevelChunkSectionExtension)sections[sectionIndex]).fluidloggable$getFluidStates();
		if (!source.isEmpty()) {
			Short2ObjectOpenHashMap<FluidState> copy = new Short2ObjectOpenHashMap<>(source);
			copy.defaultReturnValue(Fluids.EMPTY.defaultFluidState());
			this.fluidloggable$fluidStates = copy;
		}
	}

	@Override
	public FluidState fluidloggable$getFluidState(final BlockPos pos) {
		FluidState stored = this.fluidloggable$fluidStates == null
			? Fluids.EMPTY.defaultFluidState()
			: this.fluidloggable$fluidStates.get(fluidloggable$packLocalPos(pos));
		if (!stored.isEmpty()) {
			return stored;
		}

		BlockState blockState = ((SectionCopy) (Object) this).getBlockState(pos);
		return blockState.getFluidState();
	}

	@Unique
	private static short fluidloggable$packLocalPos(final BlockPos pos) {
		return (short)((pos.getX() & 15) << 8 | (pos.getY() & 15) << 4 | pos.getZ() & 15);
	}
}
