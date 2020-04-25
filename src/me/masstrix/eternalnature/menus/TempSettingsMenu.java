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

        setButton(new Button(getInventory(), asSlot(1, 3), () -> new ItemBuilder(Material.IRON_SWORD)
                .setName("&a" + le.getText("menu.temp.damage.title"))
                .addDescription(le.getText("menu.temp.damage.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_DAMAGE))
                .build()).setToggle(le.getText("menu.temp.damage.title"),
                () -> config.isEnabled(ConfigOption.TEMPERATURE_DAMAGE))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_DAMAGE);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 4), () -> new ItemBuilder(Material.POTION)
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

        setButton(new Button(getInventory(), asSlot(1, 5), () -> new ItemBuilder(Material.FLINT_AND_STEEL)
                .setName("&a" + le.getText("menu.temp.burning.title"))
                .addDescription(le.getText("menu.temp.burning.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_BURN))
                .build()).setToggle(le.getText("menu.temp.burning.title"),
                () -> config.isEnabled(ConfigOption.TEMPERATURE_BURN))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_BURN);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 6), () -> new ItemBuilder(Material.ICE)
                .setName("&a" + le.getText("menu.temp.freezing.title"))
                .addDescription(le.getText("menu.temp.freezing.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_FREEZE))
                .build()).setToggle(le.getText("menu.temp.freezing.title"),
                () -> config.isEnabled(ConfigOption.TEMPERATURE_FREEZE))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_FREEZE);
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
    }
}
