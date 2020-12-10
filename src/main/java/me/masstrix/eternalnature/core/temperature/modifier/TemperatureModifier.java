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

package me.masstrix.eternalnature.core.temperature.modifier;

import org.bukkit.configuration.ConfigurationSection;

public interface TemperatureModifier {

    /**
     * @return the emission value for the modifier.
     */
    double getEmission();

    /**
     * Returns if a configuration section in the type matches.
     *
     * @param section a configuration section to check in.
     * @param key     the key in the config section to check if it matches this type.
     * @return if the given configuration section
     */
    default boolean doesMatchType(ConfigurationSection section, String key) {
        return false;
    }
}
