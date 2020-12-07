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

@ActionbarItem.Before("temperature")
@Configurable.Path("hydration")
public class HydrationRenderer implements StatRenderer {

    private final UserData USER;
    private final Player player;
    private BaseComponent[] barText;
    private StatusRenderMethod renderMethod = StatusRenderMethod.BOSSBAR;
    private BossBar bossBar;
    private boolean isEnabled;
    private boolean warningFlash;
    private float hydration;

    public HydrationRenderer(Player player, UserData data) {
        this.player = player;
        this.USER = data;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        StatusRenderMethod beforeMethod = renderMethod;
        isEnabled = section.getBoolean("enabled") && section.getBoolean("display.enabled");
        renderMethod = StatusRenderMethod.valueOf(section.getString("display.style"));
        warningFlash = section.getBoolean("display.warning-flash");

        if (beforeMethod != renderMethod) {
            reset();
            USER.ACTIONBAR.prepare();
        }
    }

    @Override
    public String getName() {
        return "hydration";
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

        double hydration = USER.getHydration();
        boolean flash = warningFlash && hydration <= 4 && FLASH.update();
        boolean isThirsty = USER.isThirsty();

        if (renderMethod == StatusRenderMethod.BOSSBAR) {
            if (bossBar == null) {
                bossBar = Bukkit.createBossBar("Hydration", BarColor.BLUE, BarStyle.SEGMENTED_10);
                bossBar.addPlayer(player);
            }

            bossBar.setProgress(Math.abs(hydration / 20));
            int percent = (int) ((hydration / 20F) * 100);
            StringBuilder text = new StringBuilder();

            text.append("H²O ");
            text.append(flash ? "&c" : "&f").append(percent).append("%");

            if (isThirsty)
                text.append(String.format(" &7(&aThirst Effect &7%s)", TIME_FORMAT.format(USER.getThirstTime())));

            bossBar.setTitle(StringUtil.color(text.toString()));
            return;
        } else if (bossBar != null){
            bossBar.removeAll();
            bossBar = null;
        }

        if (renderMethod == StatusRenderMethod.ACTIONBAR) {
            if (this.hydration == hydration && hydration > 4) return;
            ComponentBuilder builder = new ComponentBuilder();
            builder.color(flash ? ChatColor.RED : ChatColor.WHITE);
            builder.append("H²O ");

            float mid = Math.round(hydration / 2);
            String bubble = String.valueOf('\u2B58'); // Unicode 11096
            for (int i = 0; i < 10; i++) {
                builder.append(bubble);
                if (i < mid) {
                    if (isThirsty) builder.color(ChatColor.GREEN);
                    else builder.color(ChatColor.AQUA);
                } else if (i > mid) {
                    builder.append("\u00A78");
                } else {
                    if (isThirsty) builder.color(ChatColor.DARK_GREEN);
                    else builder.color(ChatColor.DARK_AQUA);
                }
            }
            barText = builder.create();
            USER.ACTIONBAR.prepare();
            return;
        }

        if (renderMethod == StatusRenderMethod.XP_BAR) {
            float percentage = (float) (hydration / 20F);
            player.setExp(MathUtil.minMax(percentage, 0, 1));
            player.setLevel(0);
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
