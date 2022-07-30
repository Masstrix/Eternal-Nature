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

package me.masstrix.eternalnature.core.temperature.maps;

import me.masstrix.eternalnature.core.temperature.TempModifierType;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.temperature.modifier.BiomeModifier;
import me.masstrix.eternalnature.util.WorldTime;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;

public class BiomeModifierMap extends TemperatureModifierMap<Biome> {

    public BiomeModifierMap(TemperatureProfile profile) {
        super(profile, TempModifierType.BIOME);
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        for (Biome biome : Biome.values()) {
            String match = findMatchingKey(biome.name(), section);
            if (match == null) continue;
            BiomeModifier modifier = loadBiome(section, match, biome.name());
            setModifier(biome, modifier);
        }
    }

    @Override
    public BiomeModifier getModifier(Biome biome) {
        return (BiomeModifier) MODIFIERS.getOrDefault(biome, PROFILE.getDefaultBiomeModifier());
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
                if (NumberUtils.isCreatable(time)) { // Load exact time
                    timeExact = Integer.parseInt(time);
                } else { // Load simple time
                    WorldTime wt = WorldTime.find(time);
                    timeExact = wt == null ? -1 : wt.getTime();
                }
                double emission = biomeSet.getDouble(time, 0);
                updateMinMax(emission);
                mod.put(timeExact, emission);
            }
        } else {
            mod.put(WorldTime.MID_DAY, sec.getDouble(key));
        }

        return mod;
    }
}
