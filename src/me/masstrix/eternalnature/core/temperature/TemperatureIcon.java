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

import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.ChatColor;

public enum TemperatureIcon {
    BURNING(TemperatureData.ICON_HOT, "burning", 100, ChatColor.RED),
    HOT(TemperatureData.ICON_HOT, "hot", 30, ChatColor.GOLD),
    WARM(TemperatureData.ICON_HOT, "warm", 20, ChatColor.YELLOW),
    PLEASANT(TemperatureData.ICON_NORMAL, "pleasant", 13, ChatColor.GREEN),
    COOL(TemperatureData.ICON_COLD, "cool", 5, ChatColor.DARK_AQUA),
    COLD(TemperatureData.ICON_COLD, "cold", 0, ChatColor.AQUA),
    FREEZING(TemperatureData.ICON_COLD, "freezing", -4, ChatColor.WHITE);

    private final String KEY;
    private final String NAME;
    private String icon;
    private String nameLang;
    private boolean useLang = true;
    private float temp;
    private ChatColor color;

    TemperatureIcon(String icon, String name, float temp, ChatColor color) {
        this.icon = icon;
        this.NAME = name.toUpperCase();
        this.KEY = "temp.icon." + name().toLowerCase();
        this.nameLang = this.NAME;
        this.temp = temp;
        this.color = color;
    }

    /**
     * @return the icon.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * @return the name of the icon.
     */
    public String getName() {
        return useLang ? nameLang : NAME;
    }

    /**
     * @return the temperature it should start being displayed at.
     */
    public float getTemp() {
        return temp;
    }

    /**
     * @return the color for the text.
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * Reloads all the icon names to using the current language.
     *
     * @param languageEngine language engine instance.
     */
    public static void reloadLang(LanguageEngine languageEngine) {
        for (TemperatureIcon icon : values()) {
            icon.nameLang = languageEngine.getText(icon.KEY);
            icon.useLang = icon.nameLang.length() > 0;
        }
    }
}
