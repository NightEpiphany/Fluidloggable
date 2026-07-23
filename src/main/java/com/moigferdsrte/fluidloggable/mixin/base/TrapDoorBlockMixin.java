package com.moigferdsrte.fluidloggable.mixin.base;

import com.moigferdsrte.fluidloggable.flowing.FluidFlowUpdateSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TrapDoorBlock.class)
public abstract class TrapDoorBlockMixin extends HorizontalDirectionalBlock {
	protected TrapDoorBlockMixin(final BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Inject(method = "toggle", at = @At("RETURN"))
	private void fluidloggable$scheduleFluidAfterToggle(
			final BlockState state,
			final Level level,
			final BlockPos pos,
			final @Nullable Player player,
			final CallbackInfo ci
	) {
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			FluidFlowUpdateSupport.scheduleAdjacentFluidTicks(serverLevel, pos);
		}
	}

	@Inject(method = "neighborChanged", at = @At("HEAD"))
	private void fluidloggable$scheduleFluidAfterRedstoneUpdate(
			final BlockState state,
			final Level level,
			final BlockPos pos,
			final Block block,
			final @Nullable Orientation orientation,
			final boolean movedByPiston,
			final CallbackInfo ci
	) {
		final boolean signal = level.hasNeighborSignal(pos);
		final boolean changesOpenState = signal != state.getValue(TrapDoorBlock.POWERED)
				&& signal != state.getValue(TrapDoorBlock.OPEN);
		if (changesOpenState && level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
			FluidFlowUpdateSupport.scheduleAdjacentFluidTicks(serverLevel, pos);
		}
	}
}
