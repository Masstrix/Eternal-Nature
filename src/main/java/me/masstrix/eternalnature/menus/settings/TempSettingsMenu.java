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

package me.masstrix.eternalnature.menus.settings;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.config.StatusRenderMethod;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.menus.*;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

@Configurable.Path("temperature")
public class TempSettingsMenu extends GlobalMenu {

    private MenuManager menuManager;
    private Configuration config;
    private LanguageEngine le;

    private boolean enabled;
    private boolean doDamage;
    private double damageDealt;
    private double damageDelay;
    private double coldThr;
    private double heatThr;
    private StatusRenderMethod renderMethod;
    private boolean displayEnabled;
    private boolean useRgb;

    public TempSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.TEMP_SETTINGS, 5);
        this.config = plugin.getRootConfig();
        this.menuManager = menuManager;
        this.le = plugin.getLanguageEngine();
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        doDamage = section.getBoolean("damage.enabled");
        damageDealt = section.getDouble("damage.amount");
        damageDelay = section.getInt("damage.delay");
        coldThr = section.getInt("damage.threshold.cold");
        heatThr = section.getInt("damage.threshold.heat");
        renderMethod = StatusRenderMethod.valueOf(section.getString("display.style"));
        displayEnabled = section.getBoolean("display.enabled");
        useRgb = section.getBoolean("display.use-rgb-colors", true);
        build();
    }

    @Override
    public String getTitle() {
        return le.getText("menu.temp.title");
    }

    @Override
    public void build() {
        addBackButton(menuManager, Menus.SETTINGS);

        // Enabled toggle
        setButton(new Button(asSlot(1, 2), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName(le.getText("menu.temp.enabled.title"))
                .addDescription(le.getText("menu.temp.enabled.description"))
                .addSwitch("Currently:", enabled)
                .build()).setToggle(le.getText("menu.temp.enabled.title"),
                () -> enabled)
                .onClick(player -> {
                    config.toggle("temperature.enabled");
                    config.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Temperature damage toggle
        setButton(new Button(asSlot(1, 4), () -> {
            String damageDescription = le.getText("menu.temp.damage.description");
            damageDescription = damageDescription.replace("%cold_threshold%",
                    PluginData.Colors.SECONDARY + String.valueOf(coldThr) + PluginData.Colors.MESSAGE);
            damageDescription = damageDescription.replace("%heat_threshold%",
                    PluginData.Colors.SECONDARY + String.valueOf(heatThr) + PluginData.Colors.MESSAGE);

            double delaySec = MathUtil.round(damageDelay / 1000D, 1);
            double damageRounded = MathUtil.round(damageDealt, 1);

            return new ItemBuilder(Material.IRON_SWORD)
                    .setName(le.getText("menu.temp.damage.title"))
                    .addDescription(damageDescription)
                    .addLore(le.getText("menu.temp.damage.dealt") + ": " + PluginData.Colors.SECONDARY + damageRounded)
                    .addLore(le.getText("menu.temp.damage.rate") + ": " + PluginData.Colors.SECONDARY + delaySec + "s")
                    .addLore(" ")
                    .addSwitch("Currently:", doDamage)
                    .build();
        }).setToggle(le.getText("menu.temp.damage.title"),
                () -> doDamage)
                .onClick(player -> {
                    config.toggle("temperature.damage.enabled");
                    config.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Temp Scanning menu
        setButton(new SimpleButton(asSlot(1, 6), Material.SCAFFOLDING)
                .setLangEngine(le).context("menu.temp.scanning")
                .common(SimpleButton.Common.EDIT)
                .onClick(player -> menuManager.getMenu(Menus.TEMP_SCANNING_SETTINGS).open(player)));

        setButton(new Button(asSlot(4, 4), () -> new ItemBuilder(Material.JUNGLE_SIGN)
                .setName(le.getText("menu.temp.display.title"))
                .addDescription(le.getText("menu.temp.display.description"))
                .addLore("Currently: &f" + renderMethod.getSimple(),
                        renderMethod.getDescription())
                .addAction("Click to switch to " + PluginData.Colors.MESSAGE + renderMethod.opposite().getSimple())
                .build())
                .onClick(player -> {
                    config.set("temperature.display.style", renderMethod.opposite().name());
                    config.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(4, 5), () -> {
            return new ItemBuilder(displayEnabled ? Material.LIME_BANNER : Material.GRAY_BANNER)
                    .setName(le.getText("menu.display.enabled.title"))
                    .addDescription(le.getText("menu.display.enabled.description"))
                    .addSwitch("Currently:", displayEnabled)
                    .build();
        }).onClick(player -> {
            config.toggle("temperature.display.enabled");
            config.save().reload();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));

        setButton(new Button(asSlot(4, 3), () -> {
            return new ItemBuilder(Material.RED_DYE)
                    .setName(le.getText("menu.temp.use-rgb.title"))
                    .addDescription(le.getText("menu.temp.use-rgb.description"))
                    .addSwitch("Currently:", useRgb)
                    .build();
        }).onClick(player -> {
            config.toggle("temperature.display.use-rgb-colors");
            config.save().reload();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
    }
}
