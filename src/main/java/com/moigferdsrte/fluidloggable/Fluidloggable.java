package com.moigferdsrte.fluidloggable;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import com.moigferdsrte.fluidloggable.network.ClientboundFluidUpdatePacket;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Fluidloggable implements ModInitializer {
	public static final String MOD_ID = "fluidloggable";
	public static final int UPDATE_SCHEDULE_FLUID_TICK = 0x100000;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Fluidloggable");
		PayloadTypeRegistry.clientboundPlay().register(ClientboundFluidUpdatePacket.TYPE, ClientboundFluidUpdatePacket.STREAM_CODEC);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
