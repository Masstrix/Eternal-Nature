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

import me.masstrix.eternalnature.core.temperature.maps.BiomeModifierMap;
import me.masstrix.eternalnature.core.temperature.maps.BlockModifierMap;
import me.masstrix.eternalnature.core.temperature.maps.SimpleModifierMap;
import me.masstrix.eternalnature.core.temperature.maps.TemperatureModifierMap;
import me.masstrix.eternalnature.core.world.WeatherType;
import org.bukkit.Material;

import java.util.*;

public abstract class TempModifierType {

    private final static Map<String, TempModifierType> BY_NAME = new HashMap<>();
    private final static Set<TempModifierType> VALUES = new HashSet<>();

    /**
     * @return all the modifier types.
     */
    public static Set<TempModifierType> values() {
        return Collections.unmodifiableSet(VALUES);
    }

    public static TempModifierType find(String name) {
        return BY_NAME.get(name.toLowerCase());
    }

    public static final TempModifierType BIOME = new TempModifierType("biomes") {
        @Override
        public BiomeModifierMap newMap(TemperatureProfile profile) {
            return new BiomeModifierMap(profile);
        }
    };

    public static final TempModifierType BLOCK = new TempModifierType("blocks") {
        @Override
        public BlockModifierMap newMap(TemperatureProfile profile) {
            return new BlockModifierMap(profile);
        }
    };

    public static final TempModifierType CLOTHING = new TempModifierType("clothing") {
        @Override
        public SimpleModifierMap<Material> newMap(TemperatureProfile profile) {
            return new SimpleModifierMap<>(profile, "clothing", Material::values);
        }
    };

    public static final TempModifierType WEATHER = new TempModifierType("weather") {
        @Override
        public SimpleModifierMap<WeatherType> newMap(TemperatureProfile profile) {
            return new SimpleModifierMap<>(profile, "weather", WeatherType::values);
        }
    };

    private final String PATH;

    private TempModifierType(String path) {
        this.PATH = path;
        BY_NAME.put(path.toLowerCase(), this);
        VALUES.add(this);
    }

    public String getConfigPath() {
        return PATH;
    }

    /**
     * @return a new instance of a modifier map for this type.
     */
    public abstract TemperatureModifierMap<?> newMap(TemperatureProfile profile);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TempModifierType)) return false;
        TempModifierType type = (TempModifierType) o;
        return Objects.equals(PATH, type.PATH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(PATH);
    }
}
