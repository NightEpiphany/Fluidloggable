package com.moigferdsrte.fluidloggable.network;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sent before world loading and echoed by clients that have the matching startup mode. */
public record CompatibilityPayload(int protocol) implements CustomPacketPayload {
    public static final Type<CompatibilityPayload> TYPE = new Type<>(Fluidloggable.id("compatibility"));
    public static final StreamCodec<FriendlyByteBuf, CompatibilityPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CompatibilityPayload::protocol, CompatibilityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
