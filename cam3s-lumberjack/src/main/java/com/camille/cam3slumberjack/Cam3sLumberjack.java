package com.camille.cam3slumberjack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class Cam3sLumberjack implements ModInitializer {
	public static final String MOD_ID = "cam3s_lumberjack";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModSounds.initialize();
		LumberjackProfession.initialize();
		LumberjackTrades.register();

		// Village template pools are data-driven: they are loaded per world, so the houses are added at every server start.
		ServerLifecycleEvents.SERVER_STARTING.register(VillageHouses::addToVillages);
	}
}
