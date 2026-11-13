package com.camille.cam3slumberjack;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.registry.VillagerInteractionRegistries;

import com.camille.cam3ssawmill.block.ModBlocks;

/** The lumberjack villager profession, with the sawmill as its job site. */
public final class LumberjackProfession {
	public static final ResourceKey<PoiType> POI = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, Cam3sLumberjack.id("lumberjack"));
	public static final ResourceKey<VillagerProfession> LUMBERJACK = ResourceKey.create(Registries.VILLAGER_PROFESSION, Cam3sLumberjack.id("lumberjack"));
	public static final ResourceKey<LootTable> HERO_GIFT = ResourceKey.create(Registries.LOOT_TABLE,
			Cam3sLumberjack.id("gameplay/hero_of_the_village/lumberjack_gift"));

	private LumberjackProfession() {
	}

	public static void initialize() {
		// Same values as vanilla job sites: one villager per block, found from 1 block away.
		// Referencing ModBlocks also makes sure the sawmill block is registered first.
		PointOfInterestHelper.register(POI.identifier(), 1, 1, ModBlocks.SAWMILL);

		Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, LUMBERJACK, new VillagerProfession(
				Component.translatable("entity.cam3s_lumberjack.villager.lumberjack"),
				poi -> poi.is(POI),
				poi -> poi.is(POI),
				ImmutableSet.of(),
				ImmutableSet.of(),
				ModSounds.WORK_LUMBERJACK));

		// What a lumberjack throws to a Hero of the Village.
		VillagerInteractionRegistries.registerGiftLootTable(LUMBERJACK, HERO_GIFT);
	}
}
