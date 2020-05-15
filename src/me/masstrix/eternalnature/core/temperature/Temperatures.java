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
import me.masstrix.eternalnature.util.BukkitUtil;
import me.masstrix.eternalnature.util.WorldTime;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Temperatures {

    private static final String DEF_CONFIG = "temperature-config.yml";
    private static final Pattern NUM_CHECK = Pattern.compile("[0-9]*");
    public static final String ICON_HOT = "☀";
    public static final String ICON_NORMAL = "✿";
    public static final String ICON_COLD = "❈";
    public static final String ICON_WET = "☁";

    private SystemConfig sysSonfig;
    private EternalNature plugin;
    private final boolean DEFAULT;
    private File file;
    private YamlConfiguration config;
    private Map<TempModifierType, Map<Material, TemperatureModifier>> modifiers = new HashMap<>();
    private Map<Biome, TimeTemperature> biomeModifiers = new HashMap<>();
    private double minTemp = 0;
    private double maxTemp = 1;
    private double biomeDefault;
    private double scalar;
    private double caveModifier;
    private double directSunAmplifier;
    private boolean timeModifier;

    {
        // Setup the modifier cache.
        for (TempModifierType type : TempModifierType.values()) {
            modifiers.put(type, new HashMap<>());
        }
    }

    public Temperatures(EternalNature plugin) {
        this.plugin = plugin;
        this.sysSonfig = plugin.getSystemConfig();
        DEFAULT = true;
        file =  new File(plugin.getDataFolder(), DEF_CONFIG);
    }

    public Temperatures(EternalNature plugin, String world) {
        this(plugin, getWorldFile(plugin, world));
    }

    public Temperatures(EternalNature plugin, File file) {
        this.plugin = plugin;
        this.sysSonfig = plugin.getSystemConfig();
        DEFAULT = false;
        this.file = file;
    }

    /**
     * @return if the condif file for this world exists.
     */
    public boolean hasCustomConfig() {
        return file.exists();
    }

    /**
     * Creates a config file for the worlds temperatures.
     *
     * @param replace should an existing file be replaced.
     */
    public void createFiles(boolean replace) {
        if (!replace && file.exists()) return;
        file.getParentFile().mkdirs();
        if (DEFAULT) {
            plugin.saveResource(DEF_CONFIG, true);
        }
        else {
            File destination = this.file;
            try {
                InputStream in = plugin.getResource(DEF_CONFIG);
                if (in == null) return;

                InputStreamReader streamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader);
                BufferedWriter writer = new BufferedWriter(new FileWriter(destination));

                // Write file data
                for (String line; (line = reader.readLine()) != null;) {
                    writer.write(line);
                    writer.newLine();
                }

                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads all config data into memory for this temperature set.
     */
    public void loadData() {
        if (file == null || !file.exists()) {
            return;
        }

        // Load the config file if it has not been already.
        if (config == null) {
            this.config = new YamlConfiguration();
            this.config.options().copyHeader(true);

            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        // General options
        this.scalar = config.getDouble("options.global-scalar",3);
        this.caveModifier = config.getDouble("options.cave-modifier", 0.7);
        this.directSunAmplifier = config.getDouble("options.direct-sun-amplifier", 1.3);
        this.timeModifier = config.getBoolean("options.time-modifier", true);
        this.biomeDefault = config.getDouble("options.biome-default-temp", 13D);

        if (!config.contains("data")) return;
        loadBiomes(config);
        loadMtlSection(config, TempModifierType.BLOCK);
        loadMtlSection(config, TempModifierType.CLOTHING);
    }

    /**
     * Loads a section of the config.
     *
     * @param config config to load from.
     * @param type   type of modifier to load.
     */
    private void loadMtlSection(YamlConfiguration config, TempModifierType type) {
        ConfigurationSection sec = config.getConfigurationSection("data." + type.getConfigName());
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            Material mtl = BukkitUtil.findMtl(key);
            if (mtl == null) continue;

            double[] data = new double[2];

            // Load section
            if (sec.isConfigurationSection(key)) {
                data[0] = sec.getDouble(key + ".emission", 0);
                data[1] = sec.getDouble(key + ".falloff", this.scalar);
            }
            else {
                data[0] = sec.getDouble(key, 0);
            }

            // Ignore blocks without emission
            if (data[0] == 0) continue;

            modifiers.get(type).put(mtl, type.makeModifier(data));
        }
    }

    /**
     * Loads biome temperature data from the config file.
     *
     * @param config config to load the data from.
     */
    private void loadBiomes(YamlConfiguration config) {
        ConfigurationSection sec = config.getConfigurationSection("data.biome");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            Biome biome = BukkitUtil.findBiome(key, true);
            if (biome == null) continue;

            TimeTemperature mod = new TimeTemperature();

            if (sec.isConfigurationSection(key)) {
                ConfigurationSection biomeSet = sec.getConfigurationSection(key);
                for (String time : biomeSet.getKeys(false))  {
                    int timeExact;
                    if (NUM_CHECK.matcher(time).matches()) { // Load exact time
                        timeExact = Integer.parseInt(time);
                    } else { // Load simple time
                        WorldTime wt = WorldTime.find(time);
                        timeExact = wt == null ? -1 : wt.getTime();
                    }
                    mod.put(timeExact, biomeSet.getDouble(time, 0));
                }
            } else {
               mod.put(WorldTime.MID_DAY, sec.getDouble(key));
            }

            biomeModifiers.put(biome, mod);
        }
    }

    /**
     * @return if this is the default config.
     */
    public boolean isDefaultConfig() {
        return DEFAULT;
    }

    /**
     * Returns a modifier for a material.
     *
     * @param material material to get a modifier for.
     * @param type     typr of modifier to get.
     * @return the modifier or for this material of {@code type} or null
     *         if the material has no modifiers applied to it.
     */
    public TemperatureModifier getModifier(Material material, TempModifierType type) {
        return this.modifiers.get(type).get(material);
    }

    /**
     * Returns the emission value for the material.
     *
     * @param material material to get the emission value of.
     * @param type     type of modifier this is for.
     * @return the emission value of this material or 0 if it has none.
     */
    public double getEmission(Material material, TempModifierType type) {
        TemperatureModifier mod = getModifier(material, type);
        return mod == null ? 0 : mod.getEmission();
    }

    public double getBiome(Biome biome, World world) {
        TimeTemperature mod = biomeModifiers.get(biome);
        if (mod == null) return biomeDefault;
        return mod.getLocalTemp(world);
    }

    /**
     * @return the global falloff scalar for block emissions.
     */
    public double getScalar() {
        return scalar;
    }

    /**
     * @return the cave modifier. This scales between 0 and 1.
     */
    public double getCaveModifier() {
        return caveModifier;
    }

    public double getDirectSunAmplifier() {
        return directSunAmplifier;
    }

    public boolean isTimeModifier() {
        return timeModifier;
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
        if (maxTemp < temp) {
            maxTemp = temp;
            config.set("cache.max-temp", maxTemp);
        }
        else if (temp < minTemp) {
            minTemp = temp;
            config.set("cache.min-temp", minTemp);
        }
        saveConfig();
    }

    /**
     * @return the max known temperature for this configuration. This will
     *         change as systems are run.
     */
    public double getMaxTemp() {
        return maxTemp;
    }

    /**
     * @return the min known temperature for this configuration. This will
     *         change as systems are run.
     */
    public double getMinTemp() {
        return minTemp;
    }

    /**
     * @return the default temp for a biome.
     */
    public double getBiomeDefault() {
        return biomeDefault;
    }

    /**
     * @return the freezing threshold point.
     */
    public double getBurningPoint() {
        return sysSonfig.getDouble(ConfigOption.TEMPERATURE_BURN_THR);
    }

    /**
     * @return the burning threshold point.
     */
    public double getFreezingPoint() {
        return sysSonfig.getDouble(ConfigOption.TEMPERATURE_FREEZE_THR);
    }

    /**
     * Saves any changes made to the config file.
     */
    public void saveConfig() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a file for a world.
     *
     * @param plugin instance of the plugin.
     * @param world  world to get a temperatures config for.
     * @return the temperatures config file for this world.
     */
    private static File getWorldFile(EternalNature plugin, String world) {
        File dataFolder = plugin.getDataFolder();
        File worldsFolder = new File(dataFolder, "worlds");
        File worldFolder = new File(worldsFolder, world);
        File configFile = new File(worldFolder, "temperatures.yml");
        return configFile;
    }
}
