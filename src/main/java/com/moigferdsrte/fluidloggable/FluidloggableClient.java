package com.moigferdsrte.fluidloggable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import com.moigferdsrte.fluidloggable.network.ClientboundFluidUpdatePacket;

@Environment(EnvType.CLIENT)
public final class FluidloggableClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                ClientboundFluidUpdatePacket.TYPE,
                (payload, _) -> ClientboundFluidUpdatePacket.apply(payload)
        );
    }
}
