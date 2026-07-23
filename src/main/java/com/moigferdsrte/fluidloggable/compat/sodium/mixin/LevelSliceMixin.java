package com.moigferdsrte.fluidloggable.compat.sodium.mixin;

import com.moigferdsrte.fluidloggable.compat.sodium.extension.SodiumClonedChunkSectionExtension;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice")
public class LevelSliceMixin {
	@Unique
	private static final int SECTION_ARRAY_LENGTH = 3;

	@Unique
	private static final int SECTION_ARRAY_SIZE = SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH;

	@Unique
	private static final int SECTION_BLOCK_COUNT = 16 * 16 * 16;

	@Shadow
	private int originBlockX;

	@Shadow
	private int originBlockY;

	@Shadow
	private int originBlockZ;

	@Shadow
	private BoundingBox volume;

	@Unique
	private FluidState[][] fluidloggable$fluidArrays;

	@Inject(method = "copyData", at = @At("RETURN"))
	private void fluidloggable$copyStoredFluids(final @Coerce Object context, final CallbackInfo ci) {
		if (this.fluidloggable$fluidArrays == null) {
			this.fluidloggable$fluidArrays = new FluidState[SECTION_ARRAY_SIZE][];
		} else {
			Arrays.fill(this.fluidloggable$fluidArrays, null);
		}

		Object[] sections = fluidloggable$getSections(context);
		for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
			Object section = sections[sectionIndex];
			if (!(section instanceof SodiumClonedChunkSectionExtension extension)) {
				continue;
			}

			Short2ObjectMap<FluidState> fluidStates = extension.fluidloggable$getFluidStates();
			if (fluidStates == null || fluidStates.isEmpty()) {
				continue;
			}

			FluidState[] fluidArray = new FluidState[SECTION_BLOCK_COUNT];
			for (Short2ObjectMap.Entry<FluidState> entry : fluidStates.short2ObjectEntrySet()) {
				short packedPos = entry.getShortKey();
				int localX = packedPos >> 8 & 15;
				int localY = packedPos >> 4 & 15;
				int localZ = packedPos & 15;
				fluidArray[fluidloggable$getLocalBlockIndex(localX, localY, localZ)] = entry.getValue();
			}

			this.fluidloggable$fluidArrays[sectionIndex] = fluidArray;
		}
	}

	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	private void fluidloggable$getStoredFluidState(final BlockPos pos, final CallbackInfoReturnable<FluidState> cir) {
		if (this.fluidloggable$fluidArrays == null || !this.volume.isInside(pos)) {
			return;
		}

		int relBlockX = pos.getX() - this.originBlockX;
		int relBlockY = pos.getY() - this.originBlockY;
		int relBlockZ = pos.getZ() - this.originBlockZ;
		FluidState[] fluidArray = this.fluidloggable$fluidArrays[fluidloggable$getLocalSectionIndex(relBlockX >> 4, relBlockY >> 4, relBlockZ >> 4)];
		if (fluidArray == null) {
			return;
		}

		FluidState fluidState = fluidArray[fluidloggable$getLocalBlockIndex(relBlockX & 15, relBlockY & 15, relBlockZ & 15)];
		if (fluidState != null && !fluidState.isEmpty()) {
			cir.setReturnValue(fluidState);
		}
	}

	@Unique
	private static Object[] fluidloggable$getSections(final Object context) {
		try {
			return (Object[])context.getClass().getMethod("getSections").invoke(context);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException exception) {
			throw new IllegalStateException("Unable to read Sodium chunk render context sections", exception);
		}
	}

	@Unique
	private static int fluidloggable$getLocalBlockIndex(final int blockX, final int blockY, final int blockZ) {
		return blockY << 8 | blockZ << 4 | blockX;
	}

	@Unique
	private static int fluidloggable$getLocalSectionIndex(final int sectionX, final int sectionY, final int sectionZ) {
		return sectionY * SECTION_ARRAY_LENGTH * SECTION_ARRAY_LENGTH + sectionZ * SECTION_ARRAY_LENGTH + sectionX;
	}
}
