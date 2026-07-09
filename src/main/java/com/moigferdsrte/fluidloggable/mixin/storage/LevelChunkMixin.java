package com.moigferdsrte.fluidloggable.mixin.storage;

import com.moigferdsrte.fluidloggable.block.WaterloggableBlockSupport;
import com.moigferdsrte.fluidloggable.extension.LevelChunkExtension;
import com.moigferdsrte.fluidloggable.extension.LevelChunkSectionExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jspecify.annotations.Nullable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin extends ChunkAccess implements LevelChunkExtension {
	@Shadow
	@Final
	private Level level;

	public LevelChunkMixin(
		final ChunkPos chunkPos,
		final UpgradeData upgradeData,
		final LevelHeightAccessor levelHeightAccessor,
		final PalettedContainerFactory containerFactory,
		final long inhabitedTime,
		final LevelChunkSection @Nullable [] sections,
		final @Nullable BlendingData blendingData
	) {
		super(chunkPos, upgradeData, levelHeightAccessor, containerFactory, inhabitedTime, sections, blendingData);
	}

	@Override
	public FluidState fluidloggable$setFluidState(final BlockPos pos, final FluidState fluidState) {
		int y = pos.getY();
		int sectionIndex = this.getSectionIndex(y);
		LevelChunkSection section = this.getSection(sectionIndex);
		boolean wasEmpty = section.hasOnlyAir();
		if (wasEmpty && fluidState.isEmpty()) {
			return null;
		}

		int localX = pos.getX() & 15;
		int localY = y & 15;
		int localZ = pos.getZ() & 15;
		FluidState previous = ((LevelChunkSectionExtension)section).fluidloggable$setFluidState(localX, localY, localZ, fluidState);
		if (previous == fluidState) {
			return null;
		}

		LevelChunk chunk = (LevelChunk)(Object)this;
		chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING).update(localX, y, localZ, section.getBlockState(localX, localY, localZ));
		chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(localX, y, localZ, section.getBlockState(localX, localY, localZ));
		chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR).update(localX, y, localZ, section.getBlockState(localX, localY, localZ));
		chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE).update(localX, y, localZ, section.getBlockState(localX, localY, localZ));

		boolean isEmpty = section.hasOnlyAir();
		if (wasEmpty != isEmpty) {
			this.level.getChunkSource().getLightEngine().updateSectionStatus(pos, isEmpty);
			this.level.getChunkSource().onSectionEmptinessChanged(chunk.getPos().x(), SectionPos.blockToSectionCoord(y), chunk.getPos().z(), isEmpty);
		}

		if (fluidloggable$hasDifferentLightEmission(previous, fluidState)) {
			ProfilerFiller profiler = Profiler.get();
			profiler.push("queueCheckLight");
			this.level.getChunkSource().getLightEngine().checkBlock(pos);
			profiler.pop();
		}

		this.markUnsaved();
		return previous;
	}

	@Inject(method = "setBlockState", at = @At("RETURN"))
	private void fluidloggable$clearFluidStateWhenBlockCannotStoreIt(
		final BlockPos pos,
		final BlockState state,
		final int flags,
		final CallbackInfoReturnable<BlockState> cir
	) {
		BlockState oldState = cir.getReturnValue();
		if (oldState == null) {
			return;
		}

		if (oldState.getFluidState() != state.getFluidState()) {
			fluidloggable$scheduleNearbyFluidTicks(this.level, pos);
		}

		if (!WaterloggableBlockSupport.canStoreWater(state)) {
			int y = pos.getY();
			LevelChunkSection section = this.getSection(this.getSectionIndex(y));
			((LevelChunkSectionExtension)section).fluidloggable$setFluidState(pos.getX() & 15, y & 15, pos.getZ() & 15, Fluids.EMPTY.defaultFluidState());
		} else if (WaterloggableBlockSupport.isWaterlogged(oldState)
				&& !WaterloggableBlockSupport.isWaterlogged(state)
				&& !WaterloggableBlockSupport.hasNonWaterloggedStateChange(oldState, state)) {
			int y = pos.getY();
			LevelChunkSection section = this.getSection(this.getSectionIndex(y));
			((LevelChunkSectionExtension)section).fluidloggable$setFluidState(pos.getX() & 15, y & 15, pos.getZ() & 15, Fluids.EMPTY.defaultFluidState());
		}
	}

	@Unique
	private static boolean fluidloggable$hasDifferentLightEmission(final FluidState previous, final FluidState current) {
		return false;
	}

	@Unique
	private static void fluidloggable$scheduleNearbyFluidTicks(final Level level, final BlockPos pos) {
		fluidloggable$scheduleFluidTickAt(level, pos);
		for (Direction direction : Direction.values()) {
			fluidloggable$scheduleFluidTickAt(level, pos.relative(direction));
		}
	}

	@Unique
	private static void fluidloggable$scheduleFluidTickAt(final Level level, final BlockPos pos) {
		FluidState fluidState = level.getFluidState(pos);
		if (!fluidState.isEmpty()) {
			Fluid fluid = fluidState.getType();
			level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
		}
	}

	@Unique
	private static void fluidloggable$getFluidStateTokenForVerification() {
	}
}
