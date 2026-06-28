package com.moigferdsrte.fluidloggable.mixin.storage;

import com.llamalad7.mixinextras.sugar.Local;
import com.moigferdsrte.fluidloggable.extension.LevelChunkSectionExtension;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SerializableChunkData.class)
public class SerializableChunkDataMixin {
	@Unique
	private static final String FLUIDLOGGABLE_STORED_FLUIDS_TAG = "fluidloggable:stored_fluids";

	@Shadow
	@Final
	private List<SerializableChunkData.SectionData> sectionData;

	@Redirect(
		method = "parse",
		at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/LevelChunkSection")
	)
	private static LevelChunkSection fluidloggable$readStoredFluids(
		final PalettedContainer<BlockState> states,
		final PalettedContainerRO<Holder<Biome>> biomes,
		@Local(name = "sectionTag") final CompoundTag sectionTag
	) {
		LevelChunkSection section = new LevelChunkSection(states, biomes);
		fluidloggable$readStoredFluids(sectionTag, section);
		return section;
	}

	@Inject(
		method = "write",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;",
			ordinal = 1
		)
	)
	private void fluidloggable$writeStoredFluids(
			CallbackInfoReturnable<CompoundTag> cir,
			@Local(name = "sectionTags") final ListTag sectionTags
	) {
		for (SerializableChunkData.SectionData section : this.sectionData) {
			LevelChunkSection chunkSection = section.chunkSection();
			if (chunkSection == null) {
				continue;
			}

			Short2ObjectMap<FluidState> fluidStates = ((LevelChunkSectionExtension)chunkSection).fluidloggable$getFluidStates();
			if (fluidStates.isEmpty()) {
				continue;
			}

			CompoundTag sectionTag = fluidloggable$findSectionTag(sectionTags, section.y());
			if (sectionTag != null) {
				fluidloggable$writeStoredFluids(sectionTag, fluidStates);
			}
		}
	}

	@Unique
	private static void fluidloggable$readStoredFluids(final CompoundTag sectionTag, final LevelChunkSection section) {
		int[] serialized = sectionTag.getIntArray(FLUIDLOGGABLE_STORED_FLUIDS_TAG).orElse(null);
		if (serialized == null || serialized.length < 2) {
			return;
		}

		Short2ObjectMap<FluidState> fluidStates = ((LevelChunkSectionExtension)section).fluidloggable$getFluidStates();
		fluidStates.clear();
		for (int i = 0; i + 1 < serialized.length; i += 2) {
			FluidState fluidState = Fluid.FLUID_STATE_REGISTRY.byId(serialized[i + 1]);
			if (fluidState != null && !fluidState.isEmpty()) {
				fluidStates.put((short)serialized[i], fluidState);
			}
		}
	}

	@Unique
	private static void fluidloggable$writeStoredFluids(final CompoundTag sectionTag, final Short2ObjectMap<FluidState> fluidStates) {
		int[] serialized = new int[fluidStates.size() * 2];
		int index = 0;
		for (Short2ObjectMap.Entry<FluidState> entry : fluidStates.short2ObjectEntrySet()) {
			serialized[index++] = entry.getShortKey();
			serialized[index++] = Fluid.FLUID_STATE_REGISTRY.getId(entry.getValue());
		}

		sectionTag.putIntArray(FLUIDLOGGABLE_STORED_FLUIDS_TAG, serialized);
	}

	@Unique
	private static CompoundTag fluidloggable$findSectionTag(final ListTag sectionTags, final int sectionY) {
		for (int i = 0; i < sectionTags.size(); i++) {
			CompoundTag sectionTag = sectionTags.getCompoundOrEmpty(i);
			if (sectionTag.getByteOr("Y", (byte)0) == (byte)sectionY) {
				return sectionTag;
			}
		}

		return null;
	}
}
