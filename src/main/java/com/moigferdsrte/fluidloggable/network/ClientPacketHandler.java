package com.moigferdsrte.fluidloggable.network;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.extension.LevelExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;

public final class ClientPacketHandler {
	private ClientPacketHandler() {
	}

	public static void handleFluidUpdate(final BlockPos pos, final FluidState state) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level != null) {
			((LevelExtension)level).fluidloggable$setFluid(pos, state, Block.UPDATE_CLIENTS | Fluidloggable.UPDATE_SCHEDULE_FLUID_TICK);
			level.setBlocksDirty(pos, level.getBlockState(pos), level.getBlockState(pos));
		}
	}
}
