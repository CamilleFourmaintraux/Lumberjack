# Cam3's Lumberjack

Addon for Cam3's Sawmill, Minecraft **1.21.11** (Fabric): the sawmill becomes the job site of a
new villager, the **lumberjack**, and lumberjack houses appear in villages.

## Building

Requires JDK 21 or newer, and a built Cam3's Sawmill jar:

1. In the Cam3's Sawmill project: `./gradlew build`.
2. Copy `build/libs/cam3s-sawmill-1.0.0.jar` into this project's `libs/` folder.
3. Here: `./gradlew build`. The mod jar is `build/libs/cam3s-lumberjack-1.0.0.jar`.

Install both jars, plus Fabric API, on the client and the server.

## The lumberjack

Any unemployed villager next to a sawmill becomes a lumberjack. Like every vanilla villager, it gets
two trades per level. Each lumberjack level lists exactly two, so it always gets both: a fixed trade,
and one picked at random among a few. Every level can give both a trade where the player gets emeralds
and one where the player spends them. ✦ = wood type picked per villager, see below.

| Level | Fixed trade | Random trade (one of) |
|---|---|---|
| Novice | 16 logs✦ + 1 emerald → 16 stripped logs | 40 sticks → 1 emerald · 8 logs✦ → 1 emerald · 10 charcoal → 1 emerald · 8 saplings✦ → 1 emerald |
| Apprentice | 1 iron axe → 2 emeralds | 2 emeralds → 25 planks✦ · 2 emeralds → 3 chests · 2 emeralds → 1 armor stand |
| Journeyman | 2 or 3 emeralds → enchanted wooden tool or weapon | 4 resin clumps → 1 emerald · 2 emeralds → 9 logs✦ |
| Expert | 1 diamond axe → 15 emeralds | 2 emeralds → 1 piston · 2 emeralds → 1 hanging sign✦ |
| Master | 3 emeralds → 20 rails | 3 emeralds → 1 sticky piston · 2 emeralds → 7 shelves✦ or 1 lectern · 16 bone meal → 1 emerald |

The enchanted wooden tool is a sword, axe, pickaxe, shovel or hoe with two enchantments (three, one
time in three, when the tool can take them) among Sharpness, Smite, Bane of Arthropods, Efficiency,
Unbreaking and Fortune, compatible with each other, at least one at level II. Enchantments whose
maximum is above III go up to III; Unbreaking and Fortune go up to II. It costs 2 emeralds, or 3 when
the levels add up to 5 or more.

**No loops.** An emerald buys at most 18 planks' worth of wood from a lumberjack (9 logs for 2), and
everything it buys made of planks costs more than that (40 sticks = 20 planks), so trades cannot be chained for emeralds or experience. Axes are bought below
the price vanilla villagers sell them for (iron: 3 emeralds, enchanted diamond: 17 or more).

**Wood types.** Trades marked ✦ use a wood type picked at random for each villager:

- half of the time, a wood of the villager's own biome (oak or birch in plains, spruce in snowy
  and taiga villages, acacia in savanna, acacia or jungle in desert, jungle or bamboo in jungle,
  oak or mangrove in swamp);
- otherwise, any wood its climate allows. Cold villagers (snowy, taiga) never get hot woods
  (acacia, jungle, mangrove, bamboo); hot villagers (desert, savanna, jungle) never get cold woods
  (spruce, cherry). Plains, swamp and modded villager types can get any wood.

A lumberjack also throws a few logs or sticks to a Hero of the Village. Its work sound is the
sawmill's own sound, from Cam3's Sawmill.

## Village houses

For now, a lumberjack house is a vanilla mason house whose stonecutter becomes a sawmill, facing
the same way. The vanilla structure files are not copied: the mod adds the game's own mason houses
a second time to each village house pool, with a structure processor (a `minecraft:rule` processor
list in `data/cam3s_lumberjack/worldgen/processor_list`) that swaps the block during placement.
Plains and taiga houses keep the vanilla 10 % mossy cobblestone.

| Village | Houses |
|---|---|
| Plains | `plains_masons_house_1` |
| Desert | `desert_mason_1` |
| Savanna | `savanna_mason_1` |
| Snowy | `snowy_masons_house_1`, `snowy_masons_house_2` |
| Taiga | `taiga_masons_house_1` |

Each one has weight 2, like the mason house it comes from, so villages get about as many
lumberjack houses as mason houses.

## Technical notes

- The profession, its point of interest (the sawmill's four block states) and the trades are
  registered with Fabric API.
- `minecraft:acquirable_job_site` lists the lumberjack point of interest, otherwise villagers
  would not look for sawmills.
- Houses are added to the template pools at `SERVER_STARTING` through `StructureTemplatePoolAccessor`.
