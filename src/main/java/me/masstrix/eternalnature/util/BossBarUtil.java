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

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple utility to help convert standard colors used (bukkit, hex and rgb) to
 * a boss bar color.
 *
 * @author Masstrix
 */
public class BossBarUtil {

    /*
     * This contains all possible colors for the boss bar for rgb conversions.
     */
    private static final Map<BarColor, float[]> COLORS = new HashMap<>();

    static {
        addColor(BarColor.BLUE, new Color(0, 0, 255));
        addColor(BarColor.GREEN, new Color(0, 255, 0));
        addColor(BarColor.PINK, new Color(255, 0, 190));
        addColor(BarColor.PURPLE, new Color(138, 0, 246));
        addColor(BarColor.RED, new Color(255, 11, 0));
        addColor(BarColor.YELLOW, new Color(231, 239, 0));
        addColor(BarColor.WHITE, new Color(255, 255, 255));
    }

    private static void addColor(BarColor bar, Color color) {
        float[] hsb = Color.RGBtoHSB(
                color.getRed(),
                color.getGreen(),
                color.getBlue(), null);
        COLORS.put(bar, hsb);
    }

    /**
     * Converts a rgb color into a boss bar color. This will either match to the closest hue
     * of the color or if the saturation is less than 10% return white.
     *
     * @param color color to convert.
     * @return the best matching boss bar color for the given color.
     */
    public static BarColor from(Color color) {
        return convertFromRgb(color);
    }

    /**
     * Converts a bukkit <code>ChatColor</code> to a <code>BarColor</code> used for
     * boss bars. Not all chat colours have a boss bar colour, so there is some
     * interpretation on what colour to convert it to.
     *
     * @param color color to convert.
     * @return a closest <code>BarColor</code> to color.
     */
    public static BarColor from(ChatColor color) {
        return from(color.name());
    }

    /**
     * Converts a spigot <code>ChatColor</code> to a <code>BarColor</code> used for
     * boss bars. Not all chat colours have a boss bar colour, so there is some
     * interpretation on what colour to convert it to. This also works with hex
     * based rgb colors and will convert it to the closest matching color.
     *
     * @param color color to convert.
     * @return a closest <code>BarColor</code> to color.
     */
    public static BarColor from(net.md_5.bungee.api.ChatColor color) {
        if (color.getName().startsWith("#"))
            return convertFromRgb(color.getColor());
        return from(color.getName());
    }

    /**
     * Returns a direct translation from a string to a bar color.
     *
     * @param color color to convert.
     * @return the representative bar color or white by default.
     */
    private static BarColor from(String color) {
        color = color.toUpperCase();
        switch (color) {
            case "DARK_BLUE": case "BLUE": case "DARK_AQUA": case "AQUA":
                return BarColor.BLUE;
            case "RED": case "DARK_RED":
                return BarColor.RED;
            case "YELLOW": case "GOLD":
                return BarColor.YELLOW;
            case "LIGHT_PURPLE":
                return BarColor.PINK;
            case "DARK_PURPLE":
                return BarColor.PURPLE;
            case "GREEN": case "DARK_GREEN":
                return BarColor.GREEN;
            default: return BarColor.WHITE;
        }
    }

    /**
     * Converts a rgb color into a BarColor by finding the nearest matching hue
     * to the bar. If the saturation of the color is less than 10% then it will
     * always return white otherwise it will return the nearest color.
     *
     * @param color color to convert.
     * @return the closest matching boss bar color.
     */
    public static BarColor convertFromRgb(Color color) {
        float[] hsb = Color.RGBtoHSB(
                color.getRed(),
                color.getGreen(),
                color.getBlue(), null);
        BarColor closestBar = null;
        float closest = 0;
        for (Map.Entry<BarColor, float[]> entry : COLORS.entrySet()) {
            // Only return white if the saturation is less than 10%
            if (entry.getKey() == BarColor.WHITE) {
                if (hsb[1] < 0.1) return BarColor.WHITE;
                else continue;
            }
            float diff = hueDiff(entry.getValue()[0], hsb[0]);
            if (closestBar == null || diff < closest) {
                closest = diff;
                closestBar = entry.getKey();
            }
        }
        return closestBar;
    }

    private static float hueDiff(float h1, float h2) {
        return Math.abs(h1 - h2);
    }
}
