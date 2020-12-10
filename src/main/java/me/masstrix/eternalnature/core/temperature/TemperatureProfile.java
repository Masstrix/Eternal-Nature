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

import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.temperature.maps.BiomeModifierMap;
import me.masstrix.eternalnature.core.temperature.maps.TemperatureModifierMap;
import me.masstrix.eternalnature.core.temperature.modifier.BiomeModifier;
import me.masstrix.eternalnature.core.temperature.modifier.TemperatureModifier;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Configurable.Path("temperature")
public class TemperatureProfile implements Configurable {

    private String name = "default";
    private final Configuration CONFIG;
    private final Map<TempModifierType, TemperatureModifierMap<?>> MODIFIERS;
    private final BiomeModifier DEFAULT_BIOME = new BiomeModifier(null);
    private double burnTemp;
    private double freezeTemp;
    private double defaultScalar;
    private double caveModifier;
    private double directSunAmplifier;
    private double minTemp, maxTemp;

    public TemperatureProfile(Plugin plugin, String file) {
        this(new Configuration(plugin, file));
    }

    public TemperatureProfile(Plugin plugin, File file) {
        this(new Configuration(plugin, file));
    }

    public TemperatureProfile(Configuration config) {
        this.MODIFIERS = new HashMap<>();
        CONFIG = config;

        CONFIG.subscribe(new Configurable() {
            @Override
            public void updateConfig(ConfigurationSection section) {
                caveModifier = section.getDouble("cave-modifier", 0.7);
                defaultScalar = section.getDouble("global-scalar", 4);
                directSunAmplifier = section.getDouble("direct-sun-amplifier", 1.3);
                DEFAULT_BIOME.load(section.getConfigurationSection("biome-default-temp"));
            }

            @Override
            public String getConfigPath() {
                return "options";
            }
        });

        // Register all modifier types
        for (TempModifierType type : TempModifierType.values()) {
            TemperatureModifierMap<?> map = type.newMap(this);
            MODIFIERS.put(type, map);
            CONFIG.subscribe(map);
        }
    }

    public void name(String name) {
        this.name = name;
    }

    public TemperatureProfile setDefaults(TemperatureProfile defaults) {
        CONFIG.setDefault(defaults.CONFIG);
        return this;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        burnTemp = section.getDouble("damage.threshold.heat", 60);
        freezeTemp = section.getDouble("damage.threshold.cold", -5);
        CONFIG.reload(false);

        minTemp = 0;
        maxTemp = 0;

        MODIFIERS.values().forEach(map -> {
            updateMinMax(map.getMax());
            updateMinMax(map.getMin());
        });
    }

    /**
     * Updates the min and max temperature values registered for this profile. If
     * the given value is less than the current min value or greater than the current
     * max. Eadh respective value will be set to the new value.
     *
     * @param val value to update the min max values for.
     */
    public void updateMinMax(double val) {
        if (val < minTemp) minTemp = val;
        if (val > maxTemp) maxTemp = val;
    }

    /**
     * @return the configuration for this profile.
     */
    public Configuration getConfig() {
        return CONFIG;
    }

    /**
     * Reloads this profile configuration.
     */
    public void reload() {
        CONFIG.reload(false);
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    /**
     * @return how many modifiers are registered.
     */
    public int modifierCount() {
        return MODIFIERS.size();
    }

    /**
     * @return the temperature players will start burning at.
     */
    public double getBurningPoint() {
        return burnTemp;
    }

    /**
     * @return the temperature players will start freezing at.
     */
    public double getFreezingPoint() {
        return freezeTemp;
    }

    /**
     * @return the default block emission scalar.
     */
    public double getDefaultBlockScalar() {
        return defaultScalar;
    }

    /**
     * @return the cave amplifier.
     */
    public double getCaveModifier() {
        return caveModifier;
    }

    /**
     * @return thhe direct sun amplifier.
     */
    public double getDirectSunAmplifier() {
        return directSunAmplifier;
    }

    /**
     * @return the default biome modifier.
     */
    public BiomeModifier getDefaultBiomeModifier() {
        return DEFAULT_BIOME;
    }

    public <T> TemperatureModifier getModifier(TempModifierType type, T key) {
        TemperatureModifierMap<?> unchecked = MODIFIERS.get(type);
        if (unchecked == null || !unchecked.isGenericTypeSameAs(key)) return null;
        return unchecked.getModifierUnsafe(key);
    }

    @SuppressWarnings("unchecked")
    public <T extends TemperatureModifierMap<?>> T get(TempModifierType type, T map) {
        return (T) map.getClass().cast(MODIFIERS.get(type));
    }

    public TemperatureModifierMap<?> get(TempModifierType type) {
        return MODIFIERS.get(type);
    }

    public double getEmission(TempModifierType type, Object o) {
        TemperatureModifierMap<?> map = get(type);
        if (map == null || !map.isGenericTypeSameAs(o)) return 0;
        return map.getEmissionUnsafe(o);
    }

    /**
     * Returns a biomes temperature for a specific world. The world is required
     * so the current time can be used when getting the emission. The value
     * returned is
     *
     * @param biome biome to get the emission for.
     * @param world world to get the time from.
     * @return the emission value of the biome or uses the default biome temperatures
     *         if none exist.
     */
    public double getBiome(Biome biome, World world) {
        BiomeModifierMap map = (BiomeModifierMap) MODIFIERS.get(TempModifierType.BIOME);
        BiomeModifier modifier = map.getModifier(biome);
        if (modifier == null) return DEFAULT_BIOME.getLocalTemp(world);
        return modifier.getLocalTemp(world);
    }
}
