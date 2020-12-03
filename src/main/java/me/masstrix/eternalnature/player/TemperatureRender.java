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
import me.masstrix.eternalnature.core.temperature.Temperatures;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.util.BossBarUtil;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.StringUtil;
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
    private String barText;
    private StatusRenderMethod renderMethod = StatusRenderMethod.BOSSBAR;
    private BossBar bossBar;
    private boolean isEnabled;
    private boolean warningFlash;
    private String displayFormat = "";
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

        if (beforeMethod != renderMethod) {
            reset();
            USER.ACTIONBAR.prepare();
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getActionbarText() {
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
        Temperatures temps = worldData.getTemperatures();
        TemperatureIcon icon = TemperatureIcon.getClosest(temperature, temps);

        String text = displayFormat;
        text = text.replaceAll("%temp_simple%", icon.getColor() + icon.getName() + "&f");
        text = text.replaceAll("%temp_icon%", icon.getColor() + icon.getIcon() + "&f");


        double burn = temps.getBurningPoint();
        double freeze = temps.getFreezingPoint();
        boolean willDamage = temperature <= freeze + 5 || temperature >= burn - 5;
        String tempInfoColor = flash && willDamage ? "&c" : icon.getColor().toString();

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
            bossBar.setColor(BossBarUtil.fromBukkitColor(icon.getColor()));
            return;
        } else if (bossBar != null){
            bossBar.removeAll();
            bossBar = null;
        }

        if (renderMethod == StatusRenderMethod.ACTIONBAR) {
            if (temperature == lastTemp) return;
            barText = StringUtil.color(text);
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
        barText = "";
    }
}
