/*
 * Copyright 2019 Matthew Denton
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

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;

public class BossBarUtil {

    /**
     * Converts a bukkit <code>ChatColor</code> to a <code>BarColor</code> used for
     * boss bars. Not all chat colours have a boss bar colour, so there is some
     * interpretation on what colour to convert it to.
     *
     * @param color color to convert.
     * @return a closest <code>BarColor</code> to color.
     */
    public static BarColor fromBukkitColor(ChatColor color) {
        switch (color) {
            case DARK_BLUE:
            case BLUE:
            case DARK_AQUA:
            case AQUA:
                return BarColor.BLUE;
            case RED:
            case DARK_RED:
                return BarColor.RED;
            case YELLOW:
            case GOLD:
                return BarColor.YELLOW;
            case LIGHT_PURPLE:
                return BarColor.PINK;
            case GREEN:
            case DARK_GREEN:
            return BarColor.GREEN;
            case DARK_PURPLE:
                return BarColor.PURPLE;
            default: return BarColor.WHITE;
        }
    }
}
