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

import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple utility class to help handle colors as there are a few different
 * was of referencing them.
 */
public class ColorUtil {

    private static final Map<String, ChatColor> BY_NAME = new HashMap<>();

    static {
        for (ChatColor color : ChatColor.values()) {
            String name = color.getName().toLowerCase();
            BY_NAME.put(name, color);
        }

        BY_NAME.put("pink", ChatColor.LIGHT_PURPLE);
        BY_NAME.put("cyan", ChatColor.DARK_AQUA);
        BY_NAME.put("orange", ChatColor.GOLD);
    }

    /**
     * Searches finds and returns the best matching color by name. If no color
     * is found it will default to gray.
     *
     * @param name name of color to get.
     * @return the color or gray if no matches were found.
     */
    public static ChatColor fromName(String name) {
        name = name.toLowerCase().replace(" ", "_");
        return BY_NAME.getOrDefault(name, ChatColor.GRAY);
    }
}
