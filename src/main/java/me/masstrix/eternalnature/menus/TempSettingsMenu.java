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

package me.masstrix.eternalnature.menus;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

public class TempSettingsMenu extends GlobalMenu {

    private EternalNature plugin;
    private MenuManager menuManager;
    private SystemConfig config;
    private LanguageEngine le;

    public TempSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.TEMP_SETTINGS, 5);
        this.config = plugin.getSystemConfig();
        this.menuManager = menuManager;
        this.le = plugin.getLanguageEngine();
        this.plugin = plugin;
    }

    @Override
    public String getTitle() {
        return le.getText("menu.temp.title");
    }

    @Override
    public void build() {
        addBackButton(menuManager, Menus.SETTINGS);

        // Enabled toggle
        setButton(new Button(getInventory(), asSlot(1, 2), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&a" + le.getText("menu.temp.enabled.title"))
                .addDescription(le.getText("menu.temp.enabled.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_ENABLED))
                .build()).setToggle(le.getText("menu.temp.enabled.title"),
                () -> config.isEnabled(ConfigOption.TEMPERATURE_ENABLED))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_ENABLED);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Temperature damage toggle
        setButton(new Button(getInventory(), asSlot(1, 4), () -> {
            double damageDealt = config.getDouble(ConfigOption.TEMPERATURE_DMG_AMOUNT);
            double coldThr = config.getDouble(ConfigOption.TEMPERATURE_DMG_THR_COLD);
            double heatThr = config.getDouble(ConfigOption.TEMPERATURE_DMG_THR_HEAT);
            String damageDescription = le.getText("menu.temp.damage.description");
            damageDescription = damageDescription.replace("%cold_threshold%", "&e" + coldThr + "&7");
            damageDescription = damageDescription.replace("%heat_threshold%", "&e" + heatThr + "&7");

            int damageDelay = config.getInt(ConfigOption.TEMPERATURE_DMG_DELAY);
            double delaySec = MathUtil.round(damageDelay / 1000D, 1);
            double damageRounded = MathUtil.round(damageDealt, 1);

            return new ItemBuilder(Material.IRON_SWORD)
                    .setName("&a" + le.getText("menu.temp.damage.title"))
                    .addDescription(damageDescription)
                    .addLore(le.getText("menu.temp.damage.dealt") + ": &e" + damageRounded)
                    .addLore(le.getText("menu.temp.damage.rate") + ": &e" + delaySec + "s")
                    .addLore(" ")
                    .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_DMG))
                    .build();
        }).setToggle(le.getText("menu.temp.damage.title"),
                () -> config.isEnabled(ConfigOption.TEMPERATURE_DMG))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_DMG);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Temperature Sweating toggle
        setButton(new Button(getInventory(), asSlot(1, 6), () -> new ItemBuilder(Material.POTION)
                .setPotionType(PotionType.WATER)
                .setName("&a" + le.getText("menu.temp.sweat.title"))
                .addDescription(le.getText("menu.temp.sweat.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_SWEAT))
                .build()).setToggle(le.getText("menu.temp.sweat.title"),
                () -> config.isEnabled(ConfigOption.TEMPERATURE_SWEAT))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_SWEAT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(4, 4), () -> new ItemBuilder(Material.JUNGLE_SIGN)
                .setName("&a" + le.getText("menu.temp.display.title"))
                .addDescription(le.getText("menu.temp.display.description"))
                .addLore("Currently: &f" + config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).getSimple(),
                        config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).getDescription())
                .addLore("&eClick to switch to &7" + config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).opposite().getSimple())
                .build())
                .onClick(player -> {
                    config.set(ConfigOption.TEMPERATURE_BAR_STYLE, config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).opposite().name());
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(4, 5), () -> {
            boolean isDisplayEnabled = config.isEnabled(ConfigOption.TEMPERATURE_DISPLAY_ENABLED);
            return new ItemBuilder(isDisplayEnabled ? Material.LIME_BANNER : Material.GRAY_BANNER)
                    .setName("&a" + le.getText("menu.display.enabled.title"))
                    .addDescription(le.getText("menu.display.enabled.description"))
                    .addSwitch("Currently:", isDisplayEnabled)
                    .build();
        }).onClick(player -> {
            config.toggle(ConfigOption.TEMPERATURE_DISPLAY_ENABLED);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
    }
}
