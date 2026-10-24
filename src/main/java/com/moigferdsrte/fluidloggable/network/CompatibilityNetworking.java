package com.moigferdsrte.fluidloggable.network;

import com.moigferdsrte.fluidloggable.Fluidloggable;
import com.moigferdsrte.fluidloggable.config.FluidloggableConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.FabricServerConfigurationPacketListenerImpl;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;

import java.util.function.Consumer;

public final class CompatibilityNetworking {
    private static final ConfigurationTask.Type TASK = new ConfigurationTask.Type("fluidloggable:compatibility");

    private CompatibilityNetworking() {}

    public static void register() {
        PayloadTypeRegistry.clientboundConfiguration().register(CompatibilityPayload.TYPE, CompatibilityPayload.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(CompatibilityPayload.TYPE, CompatibilityPayload.CODEC);

        // Match the startup mixin decision, including the integrated server in compatibility mode.
        boolean active = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER
                || !FluidloggableConfig.isClientOnlyCompatibilityModeEnabled();
        if (!active) {
            return;
        }

        ServerConfigurationNetworking.registerGlobalReceiver(CompatibilityPayload.TYPE, (payload, context) -> {
            if (payload.protocol() != ServerCompatibility.PROTOCOL) {
                context.packetListener().disconnect(Component.literal(
                        "Fluidloggable versions are incompatible. Install the same compatible build on client and server."));
                return;
            }
            ((FabricServerConfigurationPacketListenerImpl) context.packetListener()).completeTask(TASK);
        });

        ServerConfigurationConnectionEvents.CONFIGURE.register((listener, server) -> {
            if (!ServerConfigurationNetworking.canSend(listener, CompatibilityPayload.TYPE)) {
                listener.disconnect(Component.literal(
                        "This server requires Fluidloggable with its compatibility check. Install the matching client build, turn off Client-only Compatibility Mode, and restart Minecraft."));
                return;
            }
            ((FabricServerConfigurationPacketListenerImpl) listener).addTask(new CompatibilityTask());
        });
    }

    private record CompatibilityTask() implements ConfigurationTask {
        @Override
        public void start(Consumer<Packet<?>> sender) {
            sender.accept(ServerConfigurationNetworking.createClientboundPacket(
                    new CompatibilityPayload(ServerCompatibility.PROTOCOL)));
        }

        @Override
        public Type type() {
            return TASK;
        }
    }
}
