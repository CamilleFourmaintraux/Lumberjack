package com.camille.cam3slumberjack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;

/**
 * Lumberjack trades, using the vanilla trade rules only.
 *
 * <p>Vanilla gives a villager two trades per level, picked among the trades listed for that level;
 * when a level lists exactly two, the villager always gets both. Each lumberjack level lists exactly two:
 * <ul>
 *   <li>a fixed trade, always the same;</li>
 *   <li>a trade picked at random among several (one listing that builds one of them). Every level can
 *       give both a trade where the player gets emeralds and one where the player spends them.</li>
 * </ul>
 * Trades that use a wood type pick one at random for each villager, following {@link WoodType#randomFor}.
 *
 * <p>No trade can be looped for emeralds or experience: anything the lumberjack buys costs more planks
 * than an emerald buys from it (18 planks, as 4.5 logs), and axes are bought below vanilla selling prices.
 */
public final class LumberjackTrades {
	private static final float LOW = 0.05F;
	private static final float HIGH = 0.2F;

	private static final List<Item> WOODEN_TOOLS = List.of(
			Items.WOODEN_SWORD, Items.WOODEN_AXE, Items.WOODEN_PICKAXE, Items.WOODEN_SHOVEL, Items.WOODEN_HOE);

	/** No Mending, Silk Touch or Looting. */
	private static final List<ResourceKey<Enchantment>> WOODEN_TOOL_ENCHANTMENTS = List.of(
			Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS,
			Enchantments.EFFICIENCY, Enchantments.UNBREAKING, Enchantments.FORTUNE);

	private LumberjackTrades() {
	}

	public static void register() {
		// Novice: the player pays to strip logs, and sells one wood product.
		add(1,
				stripping(16, 1, 16, 1),
				oneOf(
						// 40 sticks = 20 planks, more than the 18 planks (4.5 logs) an emerald buys: no loop.
						buy(Items.STICK, 40, 1, 16, 2),
						buyWood(wood -> wood.log, 8, 1, 16, 2),
						// Charcoal takes a log and some fuel to make, so 10 is a bit better than selling the logs (8).
						buy(Items.CHARCOAL, 10, 1, 16, 2),
						buyWood(wood -> wood.sapling, 8, 1, 16, 2)));

		// Apprentice: the player sells an iron axe, and buys planks, chests or an armor stand.
		add(2,
				buy(Items.IRON_AXE, 1, 2, 12, 10, HIGH),
				oneOf(
						sellWood(wood -> wood.planks, 25, 2, 16, 5),
						sell(Items.CHEST, 3, 2, 12, 5),
						sell(Items.ARMOR_STAND, 1, 2, 12, 5)));

		// Journeyman: the player buys an enchanted wooden tool, and sells resin or buys logs.
		add(3,
				enchantedWoodenTool(),
				oneOf(
						// A creaking heart drops resin every night: 4 per emerald keeps it from being free money.
						buy(Items.RESIN_CLUMP, 4, 1, 12, 20),
						// 9 logs make 36 planks, 18 per emerald: everything the lumberjack buys made of planks costs more.
						sellWood(wood -> wood.log, 9, 2, 16, 10)));

		// Expert: the player sells a diamond axe, and buys a wooden item.
		add(4,
				// Below the cheapest enchanted diamond axe sold by vanilla villagers (17 emeralds), so buying one and
				// selling it back cannot be looped for experience; same idea as the iron axe (sold 3, bought 2).
				buy(Items.DIAMOND_AXE, 1, 15, 12, 30, HIGH),
				oneOf(
						sell(Items.PISTON, 1, 2, 6, 15),
						sellWood(wood -> wood.hangingSign, 1, 2, 16, 15)));

		// Master: the player buys rails, and buys a crafted item or sells bone meal.
		add(5,
				// Crafting gives 16 rails for 6 iron and an emerald is worth about 4 iron elsewhere: 20 rails for 3.
				sell(Items.RAIL, 20, 3, 12, 30),
				oneOf(
						sell(Items.STICKY_PISTON, 1, 3, 6, 30),
						shelvesOrLectern(),
						buy(Items.BONE_MEAL, 16, 1, 16, 30)));
	}

	private static void add(int level, VillagerTrades.ItemListing fixed, VillagerTrades.ItemListing random) {
		TradeOfferHelper.registerVillagerOffers(LumberjackProfession.LUMBERJACK, level, factories -> {
			factories.add(fixed);
			factories.add(random);
		});
	}

	/** One trade picked at random among several, for each villager. */
	private static VillagerTrades.ItemListing oneOf(VillagerTrades.ItemListing... choices) {
		return new Listing((level, trader, random) -> choices[random.nextInt(choices.length)].getOffer(level, trader, random));
	}

	/** The player gives {@code count} items and gets emeralds. */
	private static VillagerTrades.ItemListing buy(ItemLike item, int count, int emeralds, int maxUses, int xp) {
		return buy(item, count, emeralds, maxUses, xp, LOW);
	}

	private static VillagerTrades.ItemListing buy(ItemLike item, int count, int emeralds, int maxUses, int xp, float priceMultiplier) {
		return new Listing((level, trader, random) -> new MerchantOffer(
				new ItemCost(item, count), new ItemStack(Items.EMERALD, emeralds), maxUses, xp, priceMultiplier));
	}

	/** The player gives emeralds and gets {@code count} items. */
	private static VillagerTrades.ItemListing sell(ItemLike item, int count, int emeralds, int maxUses, int xp) {
		return new Listing((level, trader, random) -> new MerchantOffer(
				new ItemCost(Items.EMERALD, emeralds), new ItemStack(item, count), maxUses, xp, LOW));
	}

	/** The player gives {@code count} items of a random wood type (logs, saplings...) and gets emeralds. */
	private static VillagerTrades.ItemListing buyWood(Function<WoodType, Item> item, int count, int emeralds, int maxUses, int xp) {
		return new Listing((level, trader, random) -> new MerchantOffer(
				new ItemCost(item.apply(WoodType.randomFor(trader, random)), count),
				new ItemStack(Items.EMERALD, emeralds), maxUses, xp, LOW));
	}

	/** The player gives emeralds and gets {@code count} items of a random wood type (planks, boats...). */
	private static VillagerTrades.ItemListing sellWood(Function<WoodType, Item> item, int count, int emeralds, int maxUses, int xp) {
		return new Listing((level, trader, random) -> new MerchantOffer(
				new ItemCost(Items.EMERALD, emeralds), new ItemStack(item.apply(WoodType.randomFor(trader, random)), count),
				maxUses, xp, LOW));
	}

	/** The player gives logs and emeralds, and gets the same logs stripped. */
	private static VillagerTrades.ItemListing stripping(int logs, int emeralds, int maxUses, int xp) {
		return new Listing((level, trader, random) -> {
			WoodType wood = WoodType.randomFor(trader, random);
			return new MerchantOffer(new ItemCost(Items.EMERALD, emeralds), Optional.of(new ItemCost(wood.log, logs)),
					new ItemStack(wood.strippedLog, logs), maxUses, xp, LOW);
		});
	}

	/** 2 emeralds for 7 shelves of a random wood type (crafting: 1 stripped log each), or for 1 lectern. */
	private static VillagerTrades.ItemListing shelvesOrLectern() {
		return new Listing((level, trader, random) -> {
			ItemStack sold = random.nextBoolean()
					? new ItemStack(WoodType.randomFor(trader, random).shelf, 7)
					: new ItemStack(Items.LECTERN);
			return new MerchantOffer(new ItemCost(Items.EMERALD, 2), sold, 12, 30, LOW);
		});
	}

	/**
	 * A wooden tool or weapon with two enchantments (three, one time in three, when the tool can take them),
	 * all compatible with the tool and with each other, at least one of them at level II.
	 * Levels go up to III for enchantments whose maximum is above III (Sharpness, Smite, Bane of Arthropods,
	 * Efficiency), and up to II for those whose maximum is III (Unbreaking, Fortune).
	 * Price: 2 emeralds, 3 when the levels add up to 5 or more. Few uses, like vanilla enchanted tool trades.
	 */
	private static VillagerTrades.ItemListing enchantedWoodenTool() {
		return new Listing((level, trader, random) -> {
			Registry<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

			// Tools that cannot take two compatible enchantments are skipped (in vanilla, every wooden tool can).
			for (int attempt = 0; attempt < 10; attempt++) {
				ItemStack tool = new ItemStack(WOODEN_TOOLS.get(random.nextInt(WOODEN_TOOLS.size())));
				List<Holder<Enchantment>> chosen = pickEnchantments(registry, tool, random, random.nextInt(3) == 0 ? 3 : 2);

				if (chosen.size() < 2) {
					continue;
				}

				int total = 0;

				for (int i = 0; i < chosen.size(); i++) {
					int cap = levelCap(chosen.get(i).value());
					// The first one is always at least level II.
					int enchantmentLevel = i == 0 ? 2 + random.nextInt(cap - 1) : 1 + random.nextInt(cap);
					tool.enchant(chosen.get(i), enchantmentLevel);
					total += enchantmentLevel;
				}

				int price = total >= 5 ? 3 : 2;
				return new MerchantOffer(new ItemCost(Items.EMERALD, price), tool, 3, 10, HIGH);
			}

			return null;
		});
	}

	/** Up to {@code count} different enchantments that apply to the tool and are compatible with each other. */
	private static List<Holder<Enchantment>> pickEnchantments(Registry<Enchantment> registry, ItemStack tool, RandomSource random, int count) {
		List<Holder<Enchantment>> candidates = new ArrayList<>();

		for (ResourceKey<Enchantment> key : WOODEN_TOOL_ENCHANTMENTS) {
			Holder<Enchantment> enchantment = registry.getOrThrow(key);

			if (enchantment.value().canEnchant(tool) && levelCap(enchantment.value()) >= 2) {
				candidates.add(enchantment);
			}
		}

		List<Holder<Enchantment>> chosen = new ArrayList<>();

		while (chosen.size() < count && !candidates.isEmpty()) {
			Holder<Enchantment> picked = candidates.remove(random.nextInt(candidates.size()));

			if (chosen.stream().allMatch(other -> Enchantment.areCompatible(other, picked))) {
				chosen.add(picked);
			}
		}

		return chosen;
	}

	/** III for enchantments whose maximum is above III, otherwise one below their maximum (Unbreaking, Fortune: II). */
	private static int levelCap(Enchantment enchantment) {
		return enchantment.getMaxLevel() > 3 ? 3 : Math.max(1, enchantment.getMaxLevel() - 1);
	}

	@FunctionalInterface
	private interface OfferFactory {
		@Nullable MerchantOffer create(ServerLevel level, Entity trader, RandomSource random);
	}

	/** Builds a new offer every time a villager gets this trade, so random choices differ between villagers. */
	private record Listing(OfferFactory factory) implements VillagerTrades.ItemListing {
		@Override
		public @Nullable MerchantOffer getOffer(ServerLevel level, Entity trader, RandomSource random) {
			return this.factory.create(level, trader, random);
		}
	}
}
