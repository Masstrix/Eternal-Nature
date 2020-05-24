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
import me.masstrix.eternalnature.config.Reloadable;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.util.EnumUtils;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.eternalnature.util.WorldTime;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class Temperatures implements Reloadable {

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
    private FileConfiguration config;
    private Map<TempModifierType, Map<Material, TemperatureModifier>> modifiers = new HashMap<>();
    private Map<Biome, BiomeModifier> biomeModifiers = new HashMap<>();
    private BiomeModifier biomeDefault;
    private double minTemp = 0;
    private double maxTemp = 1;
    private double scalar;
    private double caveModifier;
    private double directSunAmplifier;

    {
        // Setup the modifier cache.
        for (TempModifierType type : TempModifierType.values()) {
            modifiers.put(type, new HashMap<>());
        }

        // Setup the config file.
        this.config = new YamlConfiguration();
        this.config.options().copyHeader(true);
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

    @Override
    public void reload() {
        loadData();
    }

    /**
     * Loads all config data into memory for this temperature set.
     */
    public void loadData() {
        if (file == null || !file.exists()) {
            return;
        }

        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // General options
        this.scalar = config.getDouble("options.global-scalar", 3);
        this.caveModifier = config.getDouble("options.cave-modifier", 0.7);
        this.directSunAmplifier = config.getDouble("options.direct-sun-amplifier", 1.3);

        // Load default biome temperature
        BiomeModifier defaultBiome = loadBiome(config.getConfigurationSection("options"),
                "biome-default-temp", "default");
        if (biomeDefault == null) {
            this.biomeDefault = new BiomeModifier("default");
            this.biomeDefault
                    .put(WorldTime.MORNING, 10)
                    .put(WorldTime.MID_DAY, 18)
                    .put(WorldTime.DUSK, 15)
                    .put(WorldTime.MID_NIGHT, 12);
        } else {
            this.biomeDefault = defaultBiome;
        }

        if (!config.contains("data")) return;

        // Loads all the temperature data into memory.
        new BukkitRunnable() {
            @Override
            public void run() {
                loadBiomes(config);
                loadMtl(config, TempModifierType.BLOCK);
                loadMtl(config, TempModifierType.CLOTHING);
            }
        }.runTaskAsynchronously(plugin);
    }

    /**
     * Loads a material based section of the config.
     *
     * @param config config to load from.
     * @param type   type of modifier to load.
     */
    private void loadMtl(FileConfiguration config, TempModifierType type) {
        ConfigurationSection sec = config.getConfigurationSection("data." + type.getConfigName());
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            Material mtl = EnumUtils.findMatch(Material.values(), key);
            if (mtl == null) continue;

            double[] data = new double[] {0, this.scalar};

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

            updateMinMaxTempCache(data[0]);
            modifiers.get(type).put(mtl, type.makeModifier(data));
        }
    }

    /**
     * Loads biome temperature data from the config file.
     *
     * @param config config to load the data from.
     */
    private void loadBiomes(FileConfiguration config) {
        ConfigurationSection sec = config.getConfigurationSection("data.biomes");
        if (sec == null) return;

        for (Biome biome : Biome.values()) {
            String match = findMatchingKey(biome.name(), sec.getKeys(false));

            if (match == null) continue;
            BiomeModifier mod = loadBiome(sec, match, biome.name());
            if (mod == null) continue;
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
     * Returns a biome temperature modifier.
     *
     * @param biome biome to get the modifier of.
     * @return the biomes modifier or null if it has none.
     */
    public BiomeModifier getModifier(Biome biome) {
        return this.biomeModifiers.getOrDefault(biome, biomeDefault);
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
        if (mod == null) return biomeDefault.getLocalTemp(world);
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
    public BiomeModifier getBiomeDefault() {
        return biomeDefault;
    }

    /**
     * @return the freezing threshold point.
     */
    public double getBurningPoint() {
        return sysSonfig.getDouble(ConfigOption.TEMPERATURE_DMG_THR_HEAT);
    }

    /**
     * @return the burning threshold point.
     */
    public double getFreezingPoint() {
        return sysSonfig.getDouble(ConfigOption.TEMPERATURE_DMG_THR_COLD);
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
     * Returns how many items have been loaded for the given
     * modifier type.
     *
     * @param type type to get size of.
     * @return the number of settings loaded.
     */
    public int count(TempModifierType type) {
        if (type == TempModifierType.BIOME)
            return biomeModifiers.size();
        return modifiers.get(type).size();
    }

    /**
     * Loads a biome temperature info from the config.
     *
     * @param sec  section the data is stored in.
     * @param key  key to get the temperature from in sec.
     * @param name name of biome.
     * @return a biome modifier or null if there was an error getting the data.
     */
    private BiomeModifier loadBiome(ConfigurationSection sec, String key, String name) {
        if (sec == null) return null;
        BiomeModifier mod = new BiomeModifier(name);
        // Load data into modifier
        if (sec.isConfigurationSection(key)) {
            ConfigurationSection biomeSet = sec.getConfigurationSection(key);
            if (biomeSet == null) return null;
            for (String time : biomeSet.getKeys(false))  {
                int timeExact;
                if (NUM_CHECK.matcher(time).matches()) { // Load exact time
                    timeExact = Integer.parseInt(time);
                } else { // Load simple time
                    WorldTime wt = WorldTime.find(time);
                    timeExact = wt == null ? -1 : wt.getTime();
                }
                double emission = biomeSet.getDouble(time, 0);
                updateMinMaxTempCache(emission);
                mod.put(timeExact, emission);
            }
        } else {
            mod.put(WorldTime.MID_DAY, sec.getDouble(key));
        }

        return mod;
    }

    /**
     * Matches a name in a collection of keys. This is case insensitive and
     * will return the closest matching key from the collection.
     *
     * @param matchTo string to match to.
     * @param keys    collection of keys to look through. If a key
     *                ends eith {@code *} then it will use a closest
     *                match. If there is no star at the end then an
     *                exact match will be looked for only.
     * @return the closest key or null if no matching key was found.
     */
    private String findMatchingKey(String matchTo, Collection<String> keys) {
        String match = null;
        int diff = -1;

        // Search for the best matching biome setting in the config.
        for (final String KEY : keys) {
            // End search if an exact match is found
            if (KEY.equalsIgnoreCase(matchTo)) {
                return KEY;
            }

            // Use a closest match.
            if (KEY.endsWith("*")) {
                String mutated = KEY.toUpperCase();
                mutated = mutated.substring(0, KEY.length() - 1);
                if (!matchTo.contains(mutated)) continue;
                if (matchTo.equalsIgnoreCase(mutated)) {
                    match = KEY;
                    break;
                }
                int d = StringUtil.distanceContains(matchTo, mutated, true);
                if (diff == -1 || d < diff) {
                    diff = d;
                    match = KEY;
                }
            }
        }
        return match;
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
