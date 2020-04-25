/*
 * Copyright 2020 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature.core.temperature;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.util.Stopwatch;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Biome;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemperatureData {

    public static final String ICON_HOT = "☀";
    public static final String ICON_NORMAL = "✿";
    public static final String ICON_COLD = "❈";
    public static final String ICON_WET = "☁";

    private EternalNature plugin;

    private Map<Material, Float> armorData = new HashMap<>();
    private Map<Material, Float> blocksData = new HashMap<>();
    private Map<Biome, Float> biomeData = new HashMap<>();
    private Map<WeatherType, Float> weatherData = new HashMap<>();

    private double maxBlock;
    private double minBlock;
    private double maxBiome;
    private double minBiome;

    private double directSunAmplifier;
    private double caveModifier;

    private int freezingPoint;
    private int burningPoint;
    private double maxTemp = 0;
    private double minTemp = 0;

    public TemperatureData(EternalNature plugin) {
        this.plugin = plugin;
        loadConfigData();
    }

    /**
     * Finds the most relevant name for the given temperature.
     *
     * @param temp temperature to evaluate.
     * @return the most relevant icon.
     */
    public TemperatureIcon getClosestIconName(double temp) {
        if (temp >= burningPoint - 4) return TemperatureIcon.BURNING;
        if (temp <= freezingPoint + 2) return TemperatureIcon.FREEZING;
        if (temp <= TemperatureIcon.COLD.getTemp()) return TemperatureIcon.COLD;
        TemperatureIcon icon = TemperatureIcon.FREEZING;
        for (TemperatureIcon i : TemperatureIcon.values()) {
            if (temp >= i.getTemp() && icon.getTemp() < i.getTemp())
                icon = i;
        }
        return icon;
    }

    /**
     * Loads the temperature-config.yml into cache.
     */
    public void loadConfigData() {
        SystemConfig systemConfig = plugin.getSystemConfig();
        freezingPoint = systemConfig.getInt(ConfigOption.TEMPERATURE_COLD_DMG);
        burningPoint = systemConfig.getInt(ConfigOption.TEMPERATURE_BURN_DMG);

        File file = new File(plugin.getDataFolder(), "temperature-config.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("temperature-config.yml", false);
        }
        FileConfiguration config = new YamlConfiguration();
        config.options().copyDefaults(true);
        config.options().copyHeader(true);

        Stopwatch timer = new Stopwatch().start();
        plugin.getLogger().info("Loading temperature data...");

        Map<String, Float> biomes = new HashMap<>();
        Map<String, Float> blocks = new HashMap<>();
        Map<String, Float> armor = new HashMap<>();
        Map<String, Float> weather = new HashMap<>();

        try {
            config.load(file);

            caveModifier = config.getDouble("options.cave-modifier", 0.7);
            directSunAmplifier = config.getDouble("options.direct-sun-amplifier", 1.3);
            for (String s : config.getKeys(true)) {
                if (s.equalsIgnoreCase("options")) continue;
                String[] keys = s.split("\\.");
                if (s.startsWith("biome")) {
                    double val = config.getDouble(s);
                    biomes.put(keys[keys.length - 1].toLowerCase(), (float) val);
                    if (val > maxBiome) maxBiome = val;
                    if (val < minBiome) minBiome = val;
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

            float biomeBase = biomes.getOrDefault("base", 13F);

            // Assign temperature to all biomes.
            for (Biome b : Biome.values()) {
                float v = getValue(biomes, b.name(), biomeBase);
                biomeData.put(b, v);
            }

            // Assign temperature to all equipment
            for (Material m : Material.values()) {
                float v = getValue(armor, m.name(), 0);
                if (v != 0)
                    armorData.put(m, v);
            }

            // Assign temperature to all materials
            for (Material m : Material.values()) {
                float v = getValue(blocks, m.name(), 0);
                if (v != 0)
                    blocksData.put(m, v);
            }

            // Assign temperature to all weather situations
            for (WeatherType w : WeatherType.values()) {
                float v = getValue(weather, w.name(), 0);
                if (v != 0)
                    weatherData.put(w, v);
            }

            minTemp = getMinBiomeTemp();
            maxTemp = getMaxBiomeTemp();

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        plugin.getLogger().info("Loaded temperature data in " + timer.stop() + "ms");
    }

    public enum DataTempType {
        BIOME, BLOCK, ARMOR, WEATHER
    }

    public int getFreezingPoint() {
        return freezingPoint;
    }

    public int getBurningPoint() {
        return burningPoint;
    }

    /**
     * Returns the amplifier used when a block is in direct sun light. This
     * will make standing outside warmer during the day.
     *
     * @return the direct sun amplifier.
     */
    public double getDirectSunAmplifier() {
        return directSunAmplifier;
    }

    public double getCaveModifier() {
        return caveModifier;
    }

    public double getMaxBlockTemp() {
        return maxBlock;
    }

    public double getMinBlockTemp() {
        return minBlock;
    }

    public double getMaxBiomeTemp() {
        return maxBiome;
    }

    public double getMinBiomeTemp() {
        return minBiome;
    }

    /**
     * Updates the cache for the min and max temperature for a player to reach. This
     * is calculated live due to some factors not being completely known such as armor
     * when loading in the values. THe min and max values are used to determine things
     * such as the boss bars percentage.
     *
     * @param temp temperature value to check for a cache update against.
     */
    public void updateMinMaxTempCache(double temp) {
        if (maxTemp < temp)
            maxTemp = temp;
        else if (temp < minTemp)
            minTemp = temp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public float getBlockEmission(Material material) {
        return blocksData.getOrDefault(material, 0F);
    }

    /**
     * Return a biomes temperatures.
     *
     * @param biome biome to get the temperature for.
     * @return the biomes assigned temperature.
     */
    public float getBiomeModifier(Biome biome) {
        return biomeData.getOrDefault(biome, 13F);
    }

    public float getWeatherModifier(WeatherType type) {
        return weatherData.getOrDefault(type, 0F);
    }

    /**
     * Return the emission value for an armor item.
     *
     * @param material material to get.
     * @return the emission value of the armor or 0 if it has no armor emission.
     */
    public float getArmorModifier(Material material) {
        return armorData.getOrDefault(material, 0F);
    }

    private float getValue(Map<String, Float> data, String key, float def) {
        key = key.toLowerCase();
        int diff = -1;
        float val = 0;
        for (Map.Entry<String, Float> entry : data.entrySet()) {
            String check = entry.getKey().toLowerCase();
            if (key.equalsIgnoreCase(check))
                return entry.getValue();
            if (!key.contains(check)) continue;
            int dis = StringUtil.distance(check, key);
            if (diff == -1 || dis < diff) {
                diff = dis;
                val = entry.getValue();
            }
        }
        return diff == -1 ? def : val;
    }
}
