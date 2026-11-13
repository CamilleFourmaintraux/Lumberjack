package com.camille.cam3slumberjack;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

import com.camille.cam3slumberjack.mixin.StructureTemplatePoolAccessor;

/**
 * Adds lumberjack houses to the house pools of the five vanilla village types.
 *
 * <p>For now, a lumberjack house is a vanilla mason house whose stonecutter becomes a sawmill
 * (same facing). The vanilla structure files are used as they are, from the game: the processor
 * lists in {@code data/cam3s_lumberjack/worldgen/processor_list} swap the block while the house is
 * placed. Plains and taiga houses also keep their vanilla 10 % mossy cobblestone.
 *
 * <p>Each house gets the same weight as the mason house it comes from (2).
 */
public final class VillageHouses {
	private static final int WEIGHT = 2;
	private static final ResourceKey<StructureProcessorList> SWAP = processors("stonecutter_to_sawmill");
	private static final ResourceKey<StructureProcessorList> SWAP_MOSSY = processors("stonecutter_to_sawmill_mossy");

	private record House(String pool, String structure, ResourceKey<StructureProcessorList> processors) {
	}

	private static final List<House> HOUSES = List.of(
			new House("village/plains/houses", "village/plains/houses/plains_masons_house_1", SWAP_MOSSY),
			new House("village/desert/houses", "village/desert/houses/desert_mason_1", SWAP),
			new House("village/savanna/houses", "village/savanna/houses/savanna_mason_1", SWAP),
			new House("village/snowy/houses", "village/snowy/houses/snowy_masons_house_1", SWAP),
			new House("village/snowy/houses", "village/snowy/houses/snowy_masons_house_2", SWAP),
			new House("village/taiga/houses", "village/taiga/houses/taiga_masons_house_1", SWAP_MOSSY));

	private VillageHouses() {
	}

	private static ResourceKey<StructureProcessorList> processors(String name) {
		return ResourceKey.create(Registries.PROCESSOR_LIST, Cam3sLumberjack.id(name));
	}

	public static void addToVillages(MinecraftServer server) {
		Registry<StructureTemplatePool> pools = server.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL);
		Registry<StructureProcessorList> processorLists = server.registryAccess().lookupOrThrow(Registries.PROCESSOR_LIST);

		for (House house : HOUSES) {
			StructureTemplatePool pool = pools.getValue(Identifier.withDefaultNamespace(house.pool()));

			if (pool == null) {
				// Removed by a data pack: nothing to add to.
				Cam3sLumberjack.LOGGER.warn("Village pool minecraft:{} not found, lumberjack house not added", house.pool());
				continue;
			}

			Holder<StructureProcessorList> processors = processorLists.getOrThrow(house.processors());
			StructurePoolElement element = StructurePoolElement
					.legacy(Identifier.withDefaultNamespace(house.structure()).toString(), processors)
					.apply(StructureTemplatePool.Projection.RIGID);
			add((StructureTemplatePoolAccessor) pool, element);
		}
	}

	private static void add(StructureTemplatePoolAccessor pool, StructurePoolElement element) {
		for (int i = 0; i < WEIGHT; i++) {
			pool.cam3sLumberjack$getTemplates().add(element);
		}

		// The list may be immutable: replace it with a copy that includes the new piece.
		List<Pair<StructurePoolElement, Integer>> raw = new ArrayList<>(pool.cam3sLumberjack$getRawTemplates());
		raw.add(Pair.of(element, WEIGHT));
		pool.cam3sLumberjack$setRawTemplates(raw);
	}
}
