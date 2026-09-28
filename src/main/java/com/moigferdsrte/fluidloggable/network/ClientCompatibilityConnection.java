package com.moigferdsrte.fluidloggable.network;

import net.minecraft.network.chat.Component;

public interface ClientCompatibilityConnection {
    ServerCompatibility fluidloggable$compatibility();

    void fluidloggable$disconnect(Component reason);
}
