package com.moigferdsrte.fluidloggable.mixin.storage;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.block.FluidloggedBlockStateSupport;
import com.moigferdsrte.fluidloggable.extension.LevelChunkExtension;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import com.moigferdsrte.fluidloggable.network.ClientboundFluidUpdatePacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelAccessor, AutoCloseable, LevelExtension {
	@Unique
	private boolean fluidloggable$syncingFluidloggedState;

	@Shadow
	public abstract LevelChunk getChunkAt(BlockPos pos);

	@Shadow
	public abstract void setBlocksDirty(BlockPos pos, BlockState oldState, BlockState newState);

	@Shadow
	public abstract void sendBlockUpdated(BlockPos pos, BlockState old, BlockState current, int updateFlags);

	@Override
	public boolean fluidloggable$setFluid(final BlockPos pos, final FluidState fluidState, final int flags, final int maxUpdateDepth) {
		Level level = (Level)(Object)this;
		if ((flags & Block.UPDATE_MOVE_BY_PISTON) != 0) {
			throw new IllegalArgumentException("Flag UPDATE_MOVE_BY_PISTON (0x40) is not permitted for fluid state updates");
		}
		if (!level.isInValidBounds(pos) || !level.isClientSide() && level.isDebug()) {
			return false;
		}

		LevelChunk chunk = level.getChunkAt(pos);
		FluidState previous = ((LevelChunkExtension)chunk).fluidloggable$setFluidState(pos, fluidState);
		final boolean fluidChanged = previous != null;

		BlockState blockState = level.getBlockState(pos);
		BlockState syncedBlockState = FluidloggedBlockStateSupport.withFluid(blockState, fluidState);
		final boolean blockStateChanged = syncedBlockState != blockState;
		if (blockStateChanged) {
			this.fluidloggable$syncingFluidloggedState = true;
			try {
				level.setBlock(pos, syncedBlockState, flags & ~Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK, maxUpdateDepth);
			} finally {
				this.fluidloggable$syncingFluidloggedState = false;
			}
			blockState = level.getBlockState(pos);
		}

		if (!fluidChanged) {
			if ((flags & Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK) != 0 && !fluidState.isEmpty()) {
				fluidloggable$scheduleFluidTickAt(level, pos);
			}
			return blockStateChanged;
		}

		this.setBlocksDirty(pos, blockState, blockState);

		if ((flags & Block.UPDATE_CLIENTS) != 0
			&& (!level.isClientSide() || (flags & Block.UPDATE_INVISIBLE) == 0)
			&& (level.isClientSide() || chunk.getFullStatus() != null && chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING))) {
			this.fluidloggable$sendFluidUpdated(pos, fluidState, flags);
		}

		if ((flags & Block.UPDATE_NEIGHBORS) != 0) {
			level.updateNeighborsAt(pos, blockState.getBlock());
			if (!level.isClientSide() && blockState.hasAnalogOutputSignal()) {
				level.updateNeighbourForOutputSignal(pos, blockState.getBlock());
			}
		}

		if ((flags & Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK) != 0 && !fluidState.isEmpty()) {
			fluidloggable$scheduleFluidTickAt(level, pos);
		}

		fluidloggable$scheduleNeighbouringFluidTicks(level, pos);

		if ((flags & Block.UPDATE_KNOWN_SHAPE) == 0 && maxUpdateDepth > 0) {
			int neighbourUpdateFlags = flags & -34;
			blockState.updateIndirectNeighbourShapes(level, pos, neighbourUpdateFlags, maxUpdateDepth - 1);
			blockState.updateNeighbourShapes(level, pos, neighbourUpdateFlags, maxUpdateDepth - 1);
			blockState.updateIndirectNeighbourShapes(level, pos, neighbourUpdateFlags, maxUpdateDepth - 1);
		}

		return true;
	}

	@ModifyVariable(
		method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
		at = @At("HEAD"),
		argsOnly = true,
		ordinal = 0
	)
	private BlockState fluidloggable$preserveFluidWhenUpdatingWaterloggableBlock(
		final BlockState newState,
		final BlockPos pos
	) {
		if (this.fluidloggable$syncingFluidloggedState) {
			return newState;
		}

		Level level = (Level)(Object)this;
		if (!level.isInValidBounds(pos) || !level.isClientSide() && level.isDebug()) {
			return newState;
		}

		BlockState oldState = level.getBlockState(pos);
		FluidState oldFluid = level.getFluidState(pos);
		if (oldFluid.isEmpty()
				|| !FluidloggedBlockStateSupport.containsFluid(oldState, oldFluid.getType())) {
			return newState;
		}

		if (FluidloggedBlockStateSupport.canStoreFluid(newState, oldFluid.getType())
				&& FluidloggedBlockStateSupport.hasNonFluidloggedStateChange(oldState, newState)) {
			return FluidloggedBlockStateSupport.preserveFluidlogged(oldState, newState);
		}

		if (newState.isAir()) {
			return oldFluid.createLegacyBlock();
		}

		return newState;
	}

	@Override
	public boolean fluidloggable$setBlockAndInsertFluidIfPossible(final BlockPos pos, final BlockState state, final int flags) {
		Level level = (Level)(Object)this;
		FluidState fluidState = level.getFluidState(pos);
		boolean success = level.setBlock(pos, state, flags);
		if (success
				&& !fluidState.isEmpty()
				&& FluidloggedBlockStateSupport.canStoreFluid(level.getBlockState(pos), fluidState.getType())) {
			this.fluidloggable$setFluid(pos, fluidState, flags | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK);
		}
		return success;
	}

	@Unique
    private void fluidloggable$sendFluidUpdated(final BlockPos pos, final FluidState fluidState, final int flags) {
		Level level = (Level)(Object)this;
		if (level instanceof ServerLevel serverLevel) {
			ClientboundFluidUpdatePacket packet = new ClientboundFluidUpdatePacket(pos, fluidState);
			ChunkPos chunkPos = new ChunkPos(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
			for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
				ServerPlayNetworking.send(player, packet);
			}
		} else {
			this.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), flags);
		}
	}

	@Unique
    private static void fluidloggable$scheduleNeighbouringFluidTicks(final Level level, final BlockPos pos) {
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
}
