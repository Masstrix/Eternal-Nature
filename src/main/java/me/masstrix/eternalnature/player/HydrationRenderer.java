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
import me.masstrix.eternalnature.util.ColorUtil;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.Pair;
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

    private final static BaseComponent[] FLASH_COLOR = new ComponentBuilder("")
            .color(ChatColor.RED)
            .create();
    private final OrderedFormat FORMAT = new OrderedFormat();
    private final UserData USER;
    private final Player player;
    private BaseComponent[] barText;
    private StatusRenderMethod renderMethod = StatusRenderMethod.BOSSBAR;
    private BossBar bossBar;
    private boolean isEnabled;
    private boolean warningFlash;
    private double hydration;
    private String icon;
    private ChatColor[] colors = new ChatColor[6];

    public HydrationRenderer(Player player, UserData data) {
        this.player = player;
        this.USER = data;

        FORMAT.registerTag("data", () -> {
            double hydration = USER.getHydration();
            if (renderMethod != StatusRenderMethod.ACTIONBAR) {
                int percent = (int) ((hydration / 20F) * 100);
                return new Pair<>(new ComponentBuilder(percent + "%").create(), percent + "%");
            }
            ComponentBuilder builder = new ComponentBuilder();
            for (int i = 0; i < 10; i++) {
                builder.append(icon);
                int pos = i * 2;
                int id = pos < hydration && pos + 1 < hydration ? 0 : pos < hydration && pos + 1 > hydration ? 1 : 2;
                builder.color(colors[USER.isThirsty() ? id + 3 : id]);
            }
            return new Pair<>(builder.create(), "");
        }).registerTag("effects", () -> {
            StringBuilder text = new StringBuilder();
            ComponentBuilder comp = new ComponentBuilder();
            if (USER.isThirsty()) {
                text.append(String.format(" &7(&aThirst &7%s)",
                        TIME_FORMAT.format(USER.getThirstTime())));
                comp.append("(").color(ChatColor.GRAY)
                        .append("Thirst ").color(ChatColor.GREEN)
                        .append(TIME_FORMAT.format(USER.getThirstTime())).color(ChatColor.GRAY)
                        .append(")");
            }
            return new Pair<>(comp.create(), text.toString());
        }).registerTag("flash", new OrderedFormat.TagFormatter() {
            @Override
            public Pair<BaseComponent[], String> getText() {
                double hydration = USER.getHydration();
                boolean flash = warningFlash && hydration <= 4 && FLASH.update();
                return new Pair<>(flash ? FLASH_COLOR : null, flash ? "&c" : "");
            }

            @Override
            public boolean copyFormattingToNextComponent() {
                return true;
            }
        });
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        StatusRenderMethod beforeMethod = renderMethod;
        isEnabled = section.getBoolean("enabled") && section.getBoolean("display.enabled");
        renderMethod = StatusRenderMethod.valueOf(section.getString("display.style"));
        warningFlash = section.getBoolean("display.warning-flash");
        icon = section.getString("display.icon.ico", "\u2B58");
        colors[0] = ColorUtil.fromName(section.getString("display.icon.normal.full"));
        colors[1] = ColorUtil.fromName(section.getString("display.icon.normal.half"));
        colors[2] = ColorUtil.fromName(section.getString("display.icon.normal.empty"));
        colors[3] = ColorUtil.fromName(section.getString("display.icon.thirsty.full"));
        colors[4] = ColorUtil.fromName(section.getString("display.icon.thirsty.half"));
        colors[5] = ColorUtil.fromName(section.getString("display.icon.thirsty.empty"));

        if (beforeMethod != renderMethod) {
            reset();
            USER.ACTIONBAR.prepare();
        }

        this.FORMAT.praseFormat(section.getString("display.format"));
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
            if (this.hydration == hydration && USER.getHydration() > 4) return;
            bossBar.setProgress(Math.abs(hydration / 20));
            bossBar.setTitle(StringUtil.color(FORMAT.getLegacy(true)));
            this.hydration = hydration;
            return;
        } else if (bossBar != null){
            bossBar.removeAll();
            bossBar = null;
        }

        if (renderMethod == StatusRenderMethod.ACTIONBAR) {
            if (this.hydration == hydration && USER.getHydration() > 4) return;
            barText = FORMAT.getFormatted(true);
            USER.ACTIONBAR.prepare();
            this.hydration = hydration;
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
