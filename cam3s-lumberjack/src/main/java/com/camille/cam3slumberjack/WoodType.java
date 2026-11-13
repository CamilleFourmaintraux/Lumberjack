package com.camille.cam3slumberjack;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * The vanilla overworld wood types a lumberjack trades, with the climate they grow in.
 * Nether stems are left out: villagers do not live in the Nether.
 */
public enum WoodType {
	OAK(Climate.TEMPERATE, Items.OAK_LOG, Items.STRIPPED_OAK_LOG, Items.OAK_PLANKS, Items.OAK_SAPLING, Items.OAK_CHEST_BOAT, Items.OAK_HANGING_SIGN, Items.OAK_SHELF),
	BIRCH(Climate.TEMPERATE, Items.BIRCH_LOG, Items.STRIPPED_BIRCH_LOG, Items.BIRCH_PLANKS, Items.BIRCH_SAPLING, Items.BIRCH_CHEST_BOAT, Items.BIRCH_HANGING_SIGN, Items.BIRCH_SHELF),
	DARK_OAK(Climate.TEMPERATE, Items.DARK_OAK_LOG, Items.STRIPPED_DARK_OAK_LOG, Items.DARK_OAK_PLANKS, Items.DARK_OAK_SAPLING, Items.DARK_OAK_CHEST_BOAT, Items.DARK_OAK_HANGING_SIGN, Items.DARK_OAK_SHELF),
	PALE_OAK(Climate.TEMPERATE, Items.PALE_OAK_LOG, Items.STRIPPED_PALE_OAK_LOG, Items.PALE_OAK_PLANKS, Items.PALE_OAK_SAPLING, Items.PALE_OAK_CHEST_BOAT, Items.PALE_OAK_HANGING_SIGN, Items.PALE_OAK_SHELF),
	SPRUCE(Climate.COLD, Items.SPRUCE_LOG, Items.STRIPPED_SPRUCE_LOG, Items.SPRUCE_PLANKS, Items.SPRUCE_SAPLING, Items.SPRUCE_CHEST_BOAT, Items.SPRUCE_HANGING_SIGN, Items.SPRUCE_SHELF),
	CHERRY(Climate.COLD, Items.CHERRY_LOG, Items.STRIPPED_CHERRY_LOG, Items.CHERRY_PLANKS, Items.CHERRY_SAPLING, Items.CHERRY_CHEST_BOAT, Items.CHERRY_HANGING_SIGN, Items.CHERRY_SHELF),
	ACACIA(Climate.HOT, Items.ACACIA_LOG, Items.STRIPPED_ACACIA_LOG, Items.ACACIA_PLANKS, Items.ACACIA_SAPLING, Items.ACACIA_CHEST_BOAT, Items.ACACIA_HANGING_SIGN, Items.ACACIA_SHELF),
	JUNGLE(Climate.HOT, Items.JUNGLE_LOG, Items.STRIPPED_JUNGLE_LOG, Items.JUNGLE_PLANKS, Items.JUNGLE_SAPLING, Items.JUNGLE_CHEST_BOAT, Items.JUNGLE_HANGING_SIGN, Items.JUNGLE_SHELF),
	MANGROVE(Climate.HOT, Items.MANGROVE_LOG, Items.STRIPPED_MANGROVE_LOG, Items.MANGROVE_PLANKS, Items.MANGROVE_PROPAGULE, Items.MANGROVE_CHEST_BOAT, Items.MANGROVE_HANGING_SIGN, Items.MANGROVE_SHELF),
	BAMBOO(Climate.HOT, Items.BAMBOO_BLOCK, Items.STRIPPED_BAMBOO_BLOCK, Items.BAMBOO_PLANKS, Items.BAMBOO, Items.BAMBOO_CHEST_RAFT, Items.BAMBOO_HANGING_SIGN, Items.BAMBOO_SHELF);

	/** Where a wood grows. A cold villager never trades hot woods, a hot villager never trades cold woods. */
	public enum Climate {
		COLD, TEMPERATE, HOT
	}

	public final Climate climate;
	public final Item log;
	public final Item strippedLog;
	public final Item planks;
	public final Item sapling;
	public final Item chestBoat;
	public final Item hangingSign;
	public final Item shelf;

	WoodType(Climate climate, Item log, Item strippedLog, Item planks, Item sapling, Item chestBoat, Item hangingSign, Item shelf) {
		this.climate = climate;
		this.log = log;
		this.strippedLog = strippedLog;
		this.planks = planks;
		this.sapling = sapling;
		this.chestBoat = chestBoat;
		this.hangingSign = hangingSign;
		this.shelf = shelf;
	}

	/**
	 * A random wood type for this villager. Half of the time it is a wood of the villager's own biome
	 * (spruce in the taiga, acacia in the savanna...), otherwise any wood its climate allows.
	 * Villagers of modded types count as temperate.
	 */
	public static WoodType randomFor(Entity trader, RandomSource random) {
		Holder<VillagerType> type = trader instanceof Villager villager ? villager.getVillagerData().type() : null;
		Climate climate = climateOf(type);

		List<WoodType> localWoods = nativeWoods(type);

		if (!localWoods.isEmpty() && random.nextBoolean()) {
			return localWoods.get(random.nextInt(localWoods.size()));
		}

		List<WoodType> allowed = Arrays.stream(values())
				.filter(wood -> wood.allowedIn(climate))
				.toList();
		return allowed.get(random.nextInt(allowed.size()));
	}

	private boolean allowedIn(Climate villagerClimate) {
		return switch (villagerClimate) {
			case COLD -> this.climate != Climate.HOT;
			case HOT -> this.climate != Climate.COLD;
			case TEMPERATE -> true;
		};
	}

	private static Climate climateOf(@Nullable Holder<VillagerType> type) {
		if (type == null) {
			return Climate.TEMPERATE;
		}

		if (type.is(VillagerType.SNOW) || type.is(VillagerType.TAIGA)) {
			return Climate.COLD;
		}

		if (type.is(VillagerType.DESERT) || type.is(VillagerType.SAVANNA) || type.is(VillagerType.JUNGLE)) {
			return Climate.HOT;
		}

		return Climate.TEMPERATE;
	}

	private static List<WoodType> nativeWoods(@Nullable Holder<VillagerType> type) {
		if (type == null) {
			return List.of();
		}

		if (type.is(VillagerType.PLAINS)) {
			return List.of(OAK, BIRCH);
		}

		if (type.is(VillagerType.SNOW) || type.is(VillagerType.TAIGA)) {
			return List.of(SPRUCE);
		}

		if (type.is(VillagerType.SAVANNA)) {
			return List.of(ACACIA);
		}

		// Deserts have no trees: their villagers get the closest warm woods.
		if (type.is(VillagerType.DESERT)) {
			return List.of(ACACIA, JUNGLE);
		}

		if (type.is(VillagerType.JUNGLE)) {
			return List.of(JUNGLE, BAMBOO);
		}

		if (type.is(VillagerType.SWAMP)) {
			return List.of(OAK, MANGROVE);
		}

		return List.of();
	}
}
