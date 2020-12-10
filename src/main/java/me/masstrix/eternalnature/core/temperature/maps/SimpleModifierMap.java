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
import me.masstrix.eternalnature.core.temperature.modifier.SimpleTemperatureMod;
import me.masstrix.eternalnature.util.ConfigUtil;
import me.masstrix.eternalnature.util.Named;
import org.bukkit.configuration.ConfigurationSection;

public class SimpleModifierMap<K> extends TemperatureModifierMap<K> {

    private final Constants<K> CONSTANTS;

    public SimpleModifierMap(TemperatureProfile profile, String path, Constants<K> constants) {
        super(profile, path);
        this.CONSTANTS = constants;
    }

    public SimpleModifierMap(TemperatureProfile profile, TempModifierType type, Constants<K> constants) {
        super(profile, type);
        this.CONSTANTS = constants;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        ConfigUtil util = new ConfigUtil(section);
        for (K constant : CONSTANTS.collect()) {
            String match = findMatchingKey(getName(constant), section);
            if (match == null || !util.isNumber(match)) continue;
            setModifier(constant, new SimpleTemperatureMod(util.getDouble(match)));
        }
    }

    /**
     * @param obj object to get the name value of.
     * @return the name value of the object or null if the method is not
     */
    private String getName(K obj) {
        if (obj instanceof Enum)
            return ((Enum<?>) obj).name();
        else if (obj instanceof Named)
            return ((Named) obj).getName();
        return null;
    }

    /**
     * Collects constant values of a map and returns them. This is used whenever
     * the modifier map needs to get all the constants of the type it is defined for,
     *
     * @param <K> type of object that is being collected.
     */
    public interface Constants<K> {

        /**
         * Collects all the constants and returns them.
         *
         * @return an array of all the constants.
         */
        K[] collect();
    }
}
