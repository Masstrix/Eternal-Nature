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

package me.masstrix.eternalnature.util;

import org.bukkit.configuration.ConfigurationSection;

public class ConfigUtil {

    public ConfigurationSection section;

    public ConfigUtil(ConfigurationSection section) {
        this.section = section;
    }

    public boolean isNumber(String key) {
        return section.isInt(key) || section.isDouble(key);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double def) {
        return isNumber(key) ? section.isInt(key) ? section.getInt(key) : section.getDouble(key) : def;
    }

    public Number getNumber(String key) {
        return isNumber(key) ? section.isInt(key) ? section.getInt(key) : section.getDouble(key) : 0;
    }
}
