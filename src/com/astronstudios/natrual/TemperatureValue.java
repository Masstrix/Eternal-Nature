package com.astronstudios.natrual;

public enum TemperatureValue {
    BEACHES(TemperatureType.BIOME, 20),
    BIRCH_FOREST(TemperatureType.BIOME, 20),
    BIRCH_FOREST_HILLS(TemperatureType.BIOME, 23),
    COLD_BEACH(TemperatureType.BIOME, -2),
    DEEP_OCEAN(TemperatureType.BIOME, -3),
    DESERT(TemperatureType.BIOME, 33),
    DESERT_HILLS(TemperatureType.BIOME, 34),
    EXTREME_HILLS(TemperatureType.BIOME, -2),
    EXTREME_HILLS_WITH_TREES(TemperatureType.BIOME, -2),
    FOREST(TemperatureType.BIOME, 18),
    FOREST_HILLS(TemperatureType.BIOME, 19),
    FROZEN_OCEAN(TemperatureType.BIOME, -3),
    FROZEN_RIVER(TemperatureType.BIOME, -4),
    HELL(TemperatureType.BIOME, 30),
    ICE_FLATS(TemperatureType.BIOME, -10),
    ICE_MOUNTAINS(TemperatureType.BIOME, -14),
    JUNGLE(TemperatureType.BIOME, 25),
    JUNGLE_EDGE(TemperatureType.BIOME, 23),
    JUNGLE_HILLS(TemperatureType.BIOME, -26),
    MESA(TemperatureType.BIOME, 26),
    MESA_CLEAR_ROCK(TemperatureType.BIOME, 28),
    MESA_ROCK(TemperatureType.BIOME, 27),
    MUSHROOM_ISLAND(TemperatureType.BIOME, 18),
    MUSHROOM_ISLAND_SHORE(TemperatureType.BIOME, 15),
    OCEAN(TemperatureType.BIOME, 5),
    PLAINS(TemperatureType.BIOME, 18),
    REDWOOD_TAIGA(TemperatureType.BIOME, 7),
    REDWOOD_TAIGA_HILLS(TemperatureType.BIOME, 8),
    RIVER(TemperatureType.BIOME, 14),
    ROOFED_FOREST(TemperatureType.BIOME, -2),
    SAVANNA(TemperatureType.BIOME, -2),
    SAVANNA_ROCK(TemperatureType.BIOME, -2),
    SKY(TemperatureType.BIOME, 13),
    SMALLER_EXTREME_HILLS(TemperatureType.BIOME, 3),
    STONE_BEACH(TemperatureType.BIOME, 15),
    SWAMPLAND(TemperatureType.BIOME, 22),
    TAIGA(TemperatureType.BIOME, 5),
    TAIGA_COLD(TemperatureType.BIOME, -3),
    TAIGA_COLD_HILLS(TemperatureType.BIOME, -4),
    TAIGA_HILLS(TemperatureType.BIOME, 6),
    VOID(TemperatureType.BIOME, -30),




    DIAMOND_HELMET(TemperatureType.ARMOR, 0),
    DIAMOND_CHESTPLATE(TemperatureType.ARMOR, 0),
    DIAMOND_LEGGINGS(TemperatureType.ARMOR, 0),
    DIAMOND_BOOTS(TemperatureType.ARMOR, 0),

    LEATHER_HELMET(TemperatureType.ARMOR, 1),
    LEATHER_CHESTPLATE(TemperatureType.ARMOR, 3),
    LEATHER_LEGGINGS(TemperatureType.ARMOR, 2),
    LEATHER_BOOTS(TemperatureType.ARMOR, 1),

    IRON_HELMET(TemperatureType.ARMOR, 1),
    IRON_CHESTPLATE(TemperatureType.ARMOR, 1),
    IRON_LEGGINGS(TemperatureType.ARMOR, 0),
    IRON_BOOTS(TemperatureType.ARMOR, -1),

    GOLD_HELMET(TemperatureType.ARMOR, 1),
    GOLD_CHESTPLATE(TemperatureType.ARMOR, -1),
    GOLD_LEGGINGS(TemperatureType.ARMOR, 0),
    GOLD_BOOTS(TemperatureType.ARMOR, 0),




    CAMPFIRE(TemperatureType.SPECIAL, 28),




    ITEM_SNOW(TemperatureType.ITEM, -2, "snow"),
    ITEM_ICE(TemperatureType.ITEM, -5, "ice"),
    ITEM_NETHER(TemperatureType.ITEM, 15, "nether"),
    ITEM_MAGMA(TemperatureType.ITEM, 50, "magma"),
    ITEM_LANTERN(TemperatureType.ITEM, 10, "jack_o_lantern"),
    ITEM_SEALANTERN(TemperatureType.ITEM, 5, "sea_lantern"),
    ITEM_GLOWSTONE(TemperatureType.ITEM, 5, "glowstone"),
    ITEM_TORCH(TemperatureType.ITEM, 12, "torch"),
    ITEM_BUKKIT_LAVA(TemperatureType.ITEM, 40, "lava"),




    BLOCK_SNOW(TemperatureType.BLOCK, -3, "snow"),
    BLOCK_ICE(TemperatureType.BLOCK, -3, "ice"),
    BLOCK_WATER(TemperatureType.BLOCK, 2, "water"),
    BLOCK_LAVA(TemperatureType.BLOCK, 300, "lava"),
    BLOCK_FIRE(TemperatureType.BLOCK, 50, "fire"),
    BLOCK_TORCH(TemperatureType.BLOCK, 40, "torch"),
    BLOCK_NETHER(TemperatureType.BLOCK, 20, "nether"),
    BLOCK_MAGMA(TemperatureType.BLOCK, 50, "magma"),
    BLOCK_LANTERN(TemperatureType.BLOCK, 40, "jack_o_lantern"),
    BLOCK_SEALANTERN(TemperatureType.BLOCK, 10, "sea_lantern"),
    BLOCK_GLOWSTONE(TemperatureType.BLOCK, 10, "glowstone");

    protected TemperatureType type = null;
    protected double attr = 0, attrNight;
    protected String name = null;

    TemperatureValue(TemperatureType type, double attr) {
        this.type = type;
        this.attr = attr;
    }

    TemperatureValue(TemperatureType type, double attr, double attrNight) {
        this.type = type;
        this.attr = attr;
        this.attrNight = attrNight;
    }

    TemperatureValue(TemperatureType type, double attr, String name) {
        this.type = type;
        this.attr = attr;
        this.name = name;
    }
}
