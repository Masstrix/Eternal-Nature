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
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

@Configurable.Path("icons.temperature")
public class TemperatureIcon implements Configurable {

    private static final Map<String, TemperatureIcon> ICONS = new HashMap<>();
    private static float coolest = Integer.MAX_VALUE;
    private static float hottest = Integer.MIN_VALUE;

    public final static TemperatureIcon BURNING = new TemperatureIcon("burning", "", 100,
            ChatColor.of(new Color(255, 62, 62)));
    public final static TemperatureIcon HOT = new TemperatureIcon("hot", "", 100,
            ChatColor.of(new Color(255, 160, 59)));
    public final static TemperatureIcon WARM = new TemperatureIcon("warm", "", 100,
            ChatColor.of(new Color(255, 251, 35)));
    public final static TemperatureIcon PLEASANT = new TemperatureIcon("pleasant", "", 100,
            ChatColor.of(new Color(149, 255, 55)));
    public final static TemperatureIcon COOL = new TemperatureIcon("cool", "", 100,
            ChatColor.of(new Color(49, 255, 231)));
    public final static TemperatureIcon COLD = new TemperatureIcon("cold", "", 100,
            ChatColor.of(new Color(43, 181, 255)));
    public final static TemperatureIcon FREEZING = new TemperatureIcon("freezing", "", 100,
            ChatColor.of(new Color(29, 67, 255)));

    private final String NAME;
    private final ChatColor COLOR;
    private String icon;
    private String nameLang;
    private float temp;

    public TemperatureIcon(String name, String icon, float temp, ChatColor color) {
        this.icon = icon;
        this.NAME = name.toUpperCase();
        this.nameLang = this.NAME;
        this.temp = temp;
        this.COLOR = color;
        ICONS.put(name, this);

        if (temp > hottest) hottest = temp;
        if (temp < coolest) coolest = temp;
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
        return nameLang;
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
        return COLOR;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        coolest = Integer.MAX_VALUE;
        hottest = Integer.MIN_VALUE;
        for (String key : section.getKeys(false)) {
            TemperatureIcon icon = ICONS.get(key);
            if (icon == null) continue;


            icon.icon = section.getString(key + ".icon", "?");
            icon.temp = section.getInt(key + ".temp", 0);

            if (icon.temp > hottest) hottest = icon.temp;
            if (icon.temp < coolest) coolest = icon.temp;
        }
    }

    @Override
    public String toString() {
        return "(" + icon + " " + nameLang + ") temp: " + temp + ", RGB: " + COLOR.getColor().getRGB();
    }

    /**
     * Reloads all the icon names to using the current language.
     *
     * @param languageEngine language engine instance.
     */
    public static void reloadLang(LanguageEngine languageEngine) {
        for (TemperatureIcon icon : ICONS.values()) {
            String text = languageEngine.getText("temp.icon." + icon.NAME);
            if (text.length() > 0 && !text.startsWith("temp.icon.")) {
                icon.nameLang = text;
            } else {
                icon.nameLang = icon.NAME;
            }
        }
    }

    public static ChatColor getGradatedColor(float temp) {
        temp = MathUtil.minMax(temp, coolest, hottest);
        float add = Math.abs(coolest);
        temp += add;
        float warmest = hottest - add;
        float complete = MathUtil.minMax(hottest * (temp / hottest), 0F, 1F);
        float hue = (1F * complete);

        Color color = Color.getHSBColor(hue, 0.8F, 1F);
        return ChatColor.of(color);
    }

    /**
     * Finds the most relevant name for the given temperature.
     *
     * @param temp temperature to evaluate.
     * @return the most relevant icon.
     */
    public static TemperatureIcon getClosest(double temp, TemperatureProfile config) {
        if (temp >= config.getBurningPoint() - 2) return TemperatureIcon.BURNING;
        if (temp <= config.getFreezingPoint() + 2) return TemperatureIcon.FREEZING;
        if (temp <= TemperatureIcon.COLD.getTemp()) return TemperatureIcon.COLD;
        TemperatureIcon icon = TemperatureIcon.BURNING;
        double selectedDiff = Double.MAX_VALUE;
        for (TemperatureIcon i : ICONS.values()) {
            if (i.temp == temp) return i;
            double diff = i.temp - temp;
            if (selectedDiff > diff) {
                icon = i;
                selectedDiff = diff;
            }
        }
        return icon;
    }
}
