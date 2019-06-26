package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemperatureData {

    private EternalNature plugin;
    private Map<String, Float> biomes = new HashMap<>();
    private Map<String, Float> blocks = new HashMap<>();
    private Map<String, Float> armor = new HashMap<>();
    private Map<String, Float> weather = new HashMap<>();

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

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
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
        int diff = -1;
        float val = Float.NEGATIVE_INFINITY;
        for (Map.Entry<String, Float> entry : data.entrySet()) {
            if (key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
            if (key.contains(entry.getKey())) {
                int dis = StringUtil.distance(entry.getKey(), key);
                if (diff == Float.NEGATIVE_INFINITY || dis < diff) {
                    diff = dis;
                    val = entry.getValue();
                }
            }
        }
        return val == Float.NEGATIVE_INFINITY ? def : val;
    }
}
