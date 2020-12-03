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
import me.masstrix.eternalnature.config.*;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

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
        setButton(new Button(getInventory(), asSlot(1, 2), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&a" + le.getText("menu.temp.enabled.title"))
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
        setButton(new Button(getInventory(), asSlot(1, 4), () -> {
            String damageDescription = le.getText("menu.temp.damage.description");
            damageDescription = damageDescription.replace("%cold_threshold%", "&e" + coldThr + "&7");
            damageDescription = damageDescription.replace("%heat_threshold%", "&e" + heatThr + "&7");

            double delaySec = MathUtil.round(damageDelay / 1000D, 1);
            double damageRounded = MathUtil.round(damageDealt, 1);

            return new ItemBuilder(Material.IRON_SWORD)
                    .setName("&a" + le.getText("menu.temp.damage.title"))
                    .addDescription(damageDescription)
                    .addLore(le.getText("menu.temp.damage.dealt") + ": &e" + damageRounded)
                    .addLore(le.getText("menu.temp.damage.rate") + ": &e" + delaySec + "s")
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

        setButton(new Button(getInventory(), asSlot(4, 4), () -> new ItemBuilder(Material.JUNGLE_SIGN)
                .setName("&a" + le.getText("menu.temp.display.title"))
                .addDescription(le.getText("menu.temp.display.description"))
                .addLore("Currently: &f" + renderMethod.getSimple(),
                        renderMethod.getDescription())
                .addLore("&eClick to switch to &7" + renderMethod.opposite().getSimple())
                .build())
                .onClick(player -> {
                    config.set("temperature.display.style", renderMethod.opposite().name());
                    config.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(4, 5), () -> {
            return new ItemBuilder(displayEnabled ? Material.LIME_BANNER : Material.GRAY_BANNER)
                    .setName("&a" + le.getText("menu.display.enabled.title"))
                    .addDescription(le.getText("menu.display.enabled.description"))
                    .addSwitch("Currently:", displayEnabled)
                    .build();
        }).onClick(player -> {
            config.toggle("temperature.display.enabled");
            config.save().reload();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
    }
}
