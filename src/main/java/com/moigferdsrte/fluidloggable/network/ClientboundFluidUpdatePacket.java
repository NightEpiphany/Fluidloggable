package com.moigferdsrte.fluidloggable.network;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.NonNull;

public record ClientboundFluidUpdatePacket(BlockPos pos, FluidState state) implements CustomPacketPayload {
	public static final Type<ClientboundFluidUpdatePacket> TYPE = new Type<>(Fluidloggable.id("fluid_update"));
	public static final StreamCodec<FriendlyByteBuf, ClientboundFluidUpdatePacket> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		ClientboundFluidUpdatePacket::pos,
		ByteBufCodecs.idMapper(Fluid.FLUID_STATE_REGISTRY),
		ClientboundFluidUpdatePacket::state,
		ClientboundFluidUpdatePacket::new
	);

	public static void apply(final ClientboundFluidUpdatePacket packet) {
		ClientPacketHandler.handleFluidUpdate(packet.pos, packet.state);
	}

	@Override
	public @NonNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
