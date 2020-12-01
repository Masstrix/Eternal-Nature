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
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;

public class HydrationSettingsMenu extends GlobalMenu {

    private EternalNature plugin;
    private MenuManager menuManager;
    private SystemConfig config;
    private LanguageEngine le;

    public HydrationSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.HYDRATION_SETTINGS, 5);
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.config = plugin.getSystemConfig();
        this.le = plugin.getLanguageEngine();
    }

    @Override
    public String getTitle() {
        return le.getText("menu.hydration.title");
    }

    @Override
    public void build() {
        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(getInventory(), asSlot(1, 2), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&a" + le.getText("menu.hydration.enabled.title"))
                .addDescription(le.getText("menu.hydration.enabled.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.HYDRATION_ENABLED))
                .build()).setToggle(le.getText("menu.hydration.enabled.title"),
                () -> config.isEnabled(ConfigOption.HYDRATION_ENABLED))
                .onClick(player -> {
                    config.toggle(ConfigOption.HYDRATION_ENABLED);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 3), () -> new ItemBuilder(Material.IRON_SWORD)
                .setName("&a" + le.getText("menu.hydration.damage.title"))
                .addDescription(le.getText("menu.hydration.damage.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.HYDRATION_DAMAGE))
                .build()).setToggle(le.getText("menu.hydration.damage.title"),
                () -> config.isEnabled(ConfigOption.HYDRATION_DAMAGE))
                .onClick(player -> {
                    config.toggle(ConfigOption.HYDRATION_DAMAGE);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 4), () -> new ItemBuilder(Material.RABBIT_FOOT)
                .setName("&a" + le.getText("menu.hydration.activity.title"))
                .addDescription(le.getText("menu.hydration.activity.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.HYDRATION_WALKING))
                .build()).setToggle(le.getText("menu.hydration.activity.title"),
                () -> config.isEnabled(ConfigOption.HYDRATION_WALKING))
                .onClick(player -> {
                    config.toggle(ConfigOption.HYDRATION_WALKING);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 5), () -> new ItemBuilder(Material.LINGERING_POTION)
                .setName("&a" + le.getText("menu.hydration.thirst.title"))
                .addDescription(le.getText("menu.hydration.thirst.description")
                        .replace("%amount%", "" + config.getDouble(ConfigOption.THIRST_MOD)))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.THIRST_EFFECT))
                .build()).setToggle(le.getText("menu.hydration.thirst.title"),
                () -> config.isEnabled(ConfigOption.THIRST_EFFECT))
                .onClick(player -> {
                    config.toggle(ConfigOption.THIRST_EFFECT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 6), () -> new ItemBuilder(Material.WATER_BUCKET)
                .setName("&a" + le.getText("menu.hydration.open_water.title"))
                .addDescription(le.getText("menu.hydration.open_water.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.DRINK_FROM_OPEN_WATER))
                .build()).setToggle(le.getText("menu.hydration.open_water.title"),
                () -> config.isEnabled(ConfigOption.DRINK_FROM_OPEN_WATER))
                .onClick(player -> {
                    config.toggle(ConfigOption.DRINK_FROM_OPEN_WATER);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(4, 4), () -> new ItemBuilder(Material.JUNGLE_SIGN)
                .setName("&a" + le.getText("menu.hydration.display.title"))
                .addDescription(le.getText("menu.hydration.display.description"))
                .addLore("Currently: &f" + config.getRenderMethod(ConfigOption.HYDRATION_BAR_STYLE).getSimple(),
                        config.getRenderMethod(ConfigOption.HYDRATION_BAR_STYLE).getDescription())
                .addLore("&eClick to switch to &7" + config.getRenderMethod(ConfigOption.HYDRATION_BAR_STYLE).next().getSimple())
                .build())
                .onClick(player -> {
                    config.set(ConfigOption.HYDRATION_BAR_STYLE, config.getRenderMethod(ConfigOption.HYDRATION_BAR_STYLE).next().name());
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(4, 5), () -> {
            boolean isDisplayEnabled = config.isEnabled(ConfigOption.HYDRATION_BAR_DISPLAY_ENABLED);
            return new ItemBuilder(isDisplayEnabled ? Material.LIME_BANNER : Material.GRAY_BANNER)
                    .setName("&a" + le.getText("menu.display.enabled.title"))
                    .addDescription(le.getText("menu.display.enabled.description"))
                    .addSwitch("Currently:", isDisplayEnabled)
                    .build();
        }).onClick(player -> {
            config.toggle(ConfigOption.HYDRATION_BAR_DISPLAY_ENABLED);
            config.save();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
    }
}
