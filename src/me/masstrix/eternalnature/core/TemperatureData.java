package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.util.Stopwatch;
import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemperatureData {

    private EternalNature plugin;
    @Deprecated
    private Map<String, Float> biomes = new HashMap<>();
    @Deprecated
    private Map<String, Float> blocks = new HashMap<>();
    @Deprecated
    private Map<String, Float> armor = new HashMap<>();
    @Deprecated
    private Map<String, Float> weather = new HashMap<>();

    private Map<Material, Float> armorMap = new HashMap<>();
    private Map<Material, Float> blocksExact = new HashMap<>();
    private Map<Biome, Float> biomeExact = new HashMap<>();

    private double maxBlock = 0;
    private double minBlock = 0;

    public TemperatureData(EternalNature plugin) {
        this.plugin = plugin;
        loadConfigData();
    }

    /**
     * Loads the temperature-config.yml into cache.
     */
    public void loadConfigData() {
        File file = new File(plugin.getDataFolder(), "temperature-config.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("temperature-config.yml", false);
        }
        YamlConfiguration config = new YamlConfiguration();
        config.options().copyDefaults(true);
        config.options().copyHeader(true);
        Stopwatch timer = new Stopwatch().start();
        plugin.getLogger().info("Loading temperature data...");
        try {
            config.load(file);
            for (String s : config.getKeys(true)) {
                String[] keys = s.split("\\.");
                if (s.startsWith("biome")) {
                    biomes.put(keys[keys.length - 1].toLowerCase(), (float) config.getDouble(s));
                }
                else if (s.startsWith("blocks")) {
                    double val = config.getDouble(s);
                    blocks.put(keys[keys.length - 1].toLowerCase(), (float) val);
                    if (val > maxBlock)
                        maxBlock = val;
                    if (val < minBlock)
                        minBlock = val;
                }
                else if (s.startsWith("armor")) {
                    armor.put(keys[keys.length - 1].toLowerCase(), (float) config.getDouble(s));
                }
                else if (s.startsWith("weather")) {
                    weather.put(keys[keys.length - 1].toLowerCase(), (float) config.getDouble(s));
                }
            }

            // Assign temperature to all biomes.
            for (Biome b : Biome.values()) {
                float v = getEmissionValue(DataTempType.BIOME, b.name());
                biomeExact.put(b, v);
            }

            // Assign temperature to all equipment
            for (Material m : Material.values()) {
                float v = getEmissionValue(DataTempType.ARMOR, m.name());
                if (v != 0)
                    armorMap.put(m, v);
            }

            // Assign temperature to all materials
            for (Material m : Material.values()) {
                float v = getEmissionValue(DataTempType.BLOCK, m.name());
                if (v != 0)
                    blocksExact.put(m, v);
            }

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("Loaded temperature data in " + timer.stop() + "ms");
    }

    public enum DataTempType {
        BIOME, BLOCK, ARMOR, WEATHER
    }

    public double getMaxBlockTemp() {
        return maxBlock;
    }

    public double getMinBlockTemp() {
        return minBlock;
    }

    public float getExactBlockEmission(Material material) {
        return blocksExact.getOrDefault(material, 0F);
    }

    /**
     * Return a biomes temperatures.
     *
     * @param biome biome to get the temperature for.
     * @return the biomes assigned temperature.
     */
    public float getExactBiomeTemp(Biome biome) {
        return biomeExact.getOrDefault(biome, 13F);
    }

    /**
     * Return the emission value for an armor item.
     *
     * @param material material to get.
     * @return the emission value of the armor or 0 if it has no armor emission.
     */
    public float getArmorEmission(Material material) {
        return armorMap.getOrDefault(material, 0F);
    }

    /**
     * Return the emission value of a given item.
     *
     * @param type type of data to look for.
     * @param key the key of the item to match for.
     * @return the emission value of the key and type or 0 if no match was found.
     */
    public float getEmissionValue(DataTempType type, String key) {
        key = key.toLowerCase();
        switch (type) {
            case BIOME: {
                float val = getValue(biomes, key);
                if (val == Float.NEGATIVE_INFINITY)
                    return biomes.getOrDefault("base", 13F);
                else return val;
            }
            case BLOCK: return getValue(blocks, key, 0);
            case ARMOR: return getValue(armor, key, 0);
            case WEATHER: return getValue(weather, key, 0);
        }
        return 0;
    }

    /**
     * Return if there are any matching emitters to that type.
     *
     * @param type type to search for.
     * @param key key to match against.
     * @return if there is a matching key that has an emission value.
     */
    public boolean doesEmit(DataTempType type, String key) {
        key = key.toLowerCase();
        switch (type) {
            case BIOME: return getValue(biomes, key, 0) != 0;
            case BLOCK: return getValue(blocks, key, 0) != 0;
            case ARMOR: return getValue(armor, key, 0) != 0;
            case WEATHER: return getValue(weather, key, 0) != 0;
        }
        return false;
    }

    private float getValue(Map<String, Float> data, String key) {
        return getValue(data, key, Float.NEGATIVE_INFINITY);
    }

    private float getValue(Map<String, Float> data, String key, float def) {
        key = key.toLowerCase();
        int diff = -1;
        float val = 0;
        for (Map.Entry<String, Float> entry : data.entrySet()) {
            String check = entry.getKey().toLowerCase();
            if (key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
            if (!key.contains(check)) {
                continue;
            }
            int dis = StringUtil.distance(check, key);
            if (diff == -1 || dis < diff) {
                diff = dis;
                val = entry.getValue();
            }
        }
        return diff == -1 ? def : val;
    }
}
