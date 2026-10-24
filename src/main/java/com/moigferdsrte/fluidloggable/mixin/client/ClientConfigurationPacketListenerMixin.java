package com.moigferdsrte.fluidloggable.mixin.client;

import com.moigferdsrte.fluidloggable.network.ClientCompatibilityConnection;
import com.moigferdsrte.fluidloggable.network.ClientCompatibilityNetworking;
import com.moigferdsrte.fluidloggable.network.ServerCompatibility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.configuration.ClientboundFinishConfigurationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConfigurationPacketListenerImpl.class)
public abstract class ClientConfigurationPacketListenerMixin extends ClientCommonPacketListenerImpl
        implements ClientCompatibilityConnection {
    @Unique private final ServerCompatibility fluidloggable$compatibility = new ServerCompatibility();

    protected ClientConfigurationPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
        super(minecraft, connection, cookie);
    }

    @Override
    public ServerCompatibility fluidloggable$compatibility() {
        return fluidloggable$compatibility;
    }

    @Override
    public void fluidloggable$disconnect(Component reason) {
        connection.disconnect(reason);
    }

    @Inject(method = "handleConfigurationFinished", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER), cancellable = true)
    private void fluidloggable$checkBeforeWorldLoading(ClientboundFinishConfigurationPacket packet, CallbackInfo ci) {
        if (!ClientCompatibilityNetworking.validate(this)) {
            ci.cancel();
        }
    }
}
