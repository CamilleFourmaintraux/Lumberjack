package com.camille.cam3slumberjack;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
	/** Played by a lumberjack working at its sawmill. */
	public static final SoundEvent WORK_LUMBERJACK = register("entity.villager.work_lumberjack");

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = Cam3sLumberjack.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void initialize() {
		// Loads the class, which registers the sounds.
	}
}
