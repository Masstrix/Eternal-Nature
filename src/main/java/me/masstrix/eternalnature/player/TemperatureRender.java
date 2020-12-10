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

package me.masstrix.eternalnature.player;

import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.StatusRenderMethod;
import me.masstrix.eternalnature.core.temperature.TemperatureIcon;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.util.BossBarUtil;
import me.masstrix.eternalnature.util.FindableMatch;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@Configurable.Path("temperature")
public class TemperatureRender implements StatRenderer {

    private final UserData USER;
    private final Player player;
    private BaseComponent[] barText;
    private StatusRenderMethod renderMethod = StatusRenderMethod.BOSSBAR;
    private BossBar bossBar;
    private boolean isEnabled;
    private boolean warningFlash;
    private boolean useRgb;
    private String displayFormat = "";
    private FindableMatch.MatchMethod matchMethod;
    private double lastTemp = Integer.MAX_VALUE;

    public TemperatureRender(Player player, UserData data) {
        this.player = player;
        this.USER = data;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        StatusRenderMethod beforeMethod = renderMethod;
        isEnabled = section.getBoolean("enabled") && section.getBoolean("display.enabled");
        renderMethod = StatusRenderMethod.valueOf(section.getString("display.style"));
        warningFlash = section.getBoolean("display.warning-flash");
        displayFormat = section.getString("display.format");
        useRgb = section.getBoolean("display.use-rgb-colors", true);
        matchMethod = FindableMatch.MatchMethod.fromString(section.getString("display.icon-match-method"));

        if (beforeMethod != renderMethod) {
            reset();
            USER.ACTIONBAR.prepare();
        }
    }

    @Override
    public String getName() {
        return "temperature";
    }

    @Override
    public BaseComponent[] getActionbarText() {
        return barText;
    }

    @Override
    public void render() {
        if (!isEnabled) {
            reset();
            return;
        }

        boolean flash = warningFlash && FLASH.update();
        double temperature = USER.getTemperature();

        WorldData worldData = USER.getWorld();
        if (worldData == null) return;
        TemperatureProfile temps = worldData.getTemperatures();
        TemperatureIcon icon = TemperatureIcon.find(matchMethod, temperature, temps);

        ChatColor color = icon.getColor();
        if (useRgb) color = TemperatureIcon.getGradatedColor((float) temperature);

        String text = displayFormat;
        text = text.replaceAll("%temp_simple%", color + icon.getDisplayName() + "&f");
        text = text.replaceAll("%temp_icon%", color + icon.getIcon() + "&f");

        double burn = temps.getBurningPoint();
        double freeze = temps.getFreezingPoint();
        boolean willDamage = temperature <= freeze || temperature >= burn;
        ChatColor tempInfoColor = color;
        if (warningFlash && willDamage)
            tempInfoColor = flash ? ChatColor.RED : ChatColor.WHITE;

        text = text.replaceAll("%temperature%", tempInfoColor + String.format("%.1f°", temperature));

        if (renderMethod == StatusRenderMethod.BOSSBAR) {
            if (bossBar == null) {
                bossBar = Bukkit.createBossBar("Hydration", BarColor.BLUE, BarStyle.SEGMENTED_10);
                bossBar.addPlayer(player);
            }

            // Update the bars progress
            double min = Math.abs(temps.getMinTemp());
            double max = temps.getMaxTemp() + min;
            double temp = temperature + min;
            double progress = temp / max;

            bossBar.setProgress(MathUtil.minMax(progress, 0, 1));
            bossBar.setTitle(StringUtil.color(text));
            bossBar.setColor(BossBarUtil.from(color));
            return;
        } else if (bossBar != null){
            bossBar.removeAll();
            bossBar = null;
        }

        if (renderMethod == StatusRenderMethod.ACTIONBAR) {
            if (temperature == lastTemp && !willDamage) return;

            text = displayFormat;
            String[] split = text.split(" ");

            ComponentBuilder builder = new ComponentBuilder()
                    .color(tempInfoColor);
            boolean first = true;
            for (String s : split) {
                if (!first) builder.append(" ", ComponentBuilder.FormatRetention.NONE);
                if (s.equalsIgnoreCase("%temperature%")) {
                    builder.append(String.format("%.1f°", temperature)).color(tempInfoColor);
                    first = false;
                    continue;
                }
                if (s.equalsIgnoreCase("%temp_simple%")) {
                    builder.append(icon.getDisplayName());
                }
                else if (s.equalsIgnoreCase("%temp_icon%")) {
                    builder.append(icon.getIcon());
                }
                builder.color(color).bold(false);
                first = false;
            }

            barText = builder.create();
            lastTemp = temperature;
            USER.ACTIONBAR.prepare();
        }
    }

    @Override
    public void reset() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
        barText = null;
    }
}
