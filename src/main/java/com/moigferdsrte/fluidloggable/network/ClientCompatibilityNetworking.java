package com.moigferdsrte.fluidloggable.network;

import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class ClientCompatibilityNetworking {
    private static boolean compatibilityModeAtStartup;

    private ClientCompatibilityNetworking() {}

    public static void register() {
        // Menu changes require a restart and must not change the active connection policy.
        compatibilityModeAtStartup = FluidloggableConfig.isClientOnlyCompatibilityModeEnabled();
        ClientConfigurationNetworking.registerGlobalReceiver(CompatibilityPayload.TYPE, (payload, context) -> {
            var connection = (ClientCompatibilityConnection) context.packetListener();
            connection.fluidloggable$compatibility().advertise(payload.protocol());
            if (validate(connection)) {
                ClientConfigurationNetworking.send(new CompatibilityPayload(ServerCompatibility.PROTOCOL));
            }
        });
    }

    public static boolean validate(ClientCompatibilityConnection connection) {
        ServerCompatibility.Problem problem = connection.fluidloggable$compatibility().problem(compatibilityModeAtStartup);
        if (problem == null) {
            return true;
        }
        connection.fluidloggable$disconnect(Component.translatable(problem.translationKey));
        return false;
    }
}
