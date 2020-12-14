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
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.menus.Button;
import me.masstrix.eternalnature.menus.GlobalMenu;
import me.masstrix.eternalnature.menus.MenuManager;
import me.masstrix.eternalnature.menus.Menus;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

@Configurable.Path("temperature.scanning")
public class TempScanningMenu extends GlobalMenu {

    private final MenuManager MANAGER;
    private final Configuration CONFIG;
    private final LanguageEngine LE;

    //
    // Configuration
    //
    private boolean useBlocks;
    private boolean useBiomes;
    private boolean useWeather;
    private boolean useItems;
    private boolean useEnvironment;
    private int area;
    private int height;

    public TempScanningMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.TEMP_SCANNING_SETTINGS, 5);
        this.CONFIG = plugin.getRootConfig();
        this.MANAGER = menuManager;
        this.LE = plugin.getLanguageEngine();
    }

    @Override
    public String getTitle() {
        return LE.getText("menu.temp.title");
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        useBlocks = section.getBoolean("use-blocks", true);
        useBiomes = section.getBoolean("use-blocks", true);
        useWeather = section.getBoolean("use-blocks", true);
        useItems = section.getBoolean("use-blocks", true);
        useEnvironment = section.getBoolean("use-blocks", true);
        area = section.getInt("advanced.area", 11);
        height = section.getInt("advanced.height", 5);
        build();
    }

    @Override
    public void build() {
        addBackButton(MANAGER, Menus.TEMP_SETTINGS);

        int col = 1;

        // Use Blocks
        setButton(new Button(asSlot(1, col++), () -> new ItemBuilder(Material.GRASS_BLOCK)
                .setName("&a" + LE.getText("menu.temp.scanning.use-blocks.title"))
                .addDescription(LE.getText("menu.temp.scanning.use-blocks.description"))
                .addSwitch("Currently:", useBlocks)
                .build()).setToggle(LE.getText("menu.temp.enabled.title"),
                () -> useBlocks)
                .onClick(player -> {
                    CONFIG.toggle("temperature.scanning.use-blocks");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Use Biomes
        setButton(new Button(asSlot(1, col++), () -> new ItemBuilder(Material.JUNGLE_SAPLING)
                .setName("&a" + LE.getText("menu.temp.scanning.use-biomes.title"))
                .addDescription(LE.getText("menu.temp.scanning.use-biomes.description"))
                .addSwitch("Currently:", useBiomes)
                .build()).setToggle(LE.getText("menu.temp.enabled.title"),
                () -> useBiomes)
                .onClick(player -> {
                    CONFIG.toggle("temperature.scanning.use-biomes");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Use Weather
        setButton(new Button(asSlot(1, col++), () -> new ItemBuilder(Material.WATER_BUCKET)
                .setName("&a" + LE.getText("menu.temp.scanning.use-weather.title"))
                .addDescription(LE.getText("menu.temp.scanning.use-weather.description"))
                .addSwitch("Currently:", useWeather)
                .build()).setToggle(LE.getText("menu.temp.enabled.title"),
                () -> useWeather)
                .onClick(player -> {
                    CONFIG.toggle("temperature.scanning.use-weather");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Use Items
        setButton(new Button(asSlot(1, col++), () -> new ItemBuilder(Material.LEATHER_HELMET)
                .setName("&a" + LE.getText("menu.temp.scanning.use-items.title"))
                .addDescription(LE.getText("menu.temp.scanning.use-items.description"))
                .addSwitch("Currently:", useItems)
                .build()).setToggle(LE.getText("menu.temp.enabled.title"),
                () -> useItems)
                .onClick(player -> {
                    CONFIG.toggle("temperature.scanning.use-items");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Use Environment
        setButton(new Button(asSlot(1, col), () -> new ItemBuilder(Material.SEAGRASS)
                .setName("&a" + LE.getText("menu.temp.scanning.use-environment.title"))
                .addDescription(LE.getText("menu.temp.scanning.use-environment.description"))
                .addSwitch("Currently:", useEnvironment)
                .build()).setToggle(LE.getText("menu.temp.enabled.title"),
                () -> useEnvironment)
                .onClick(player -> {
                    CONFIG.toggle("temperature.scanning.use-environment");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Area changer
        setButton(new Button(asSlot(1, 7), () -> new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                .setName("&a+")
                .build()).onClick(player -> {

        }));
        setButton(new Button(asSlot(2, 7), () -> new ItemBuilder(Material.SCAFFOLDING)
                .setName("&a" + LE.getText("menu.temp.scanning.area.title"))
                .addDescription(LE.getText("menu.temp.scanning.area.description"))
                .addLore("")
                .addLore("Currently: " + area)
                .build()));
        setButton(new Button(asSlot(3, 7), () -> new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setName("&c-")
                .build()).onClick(player -> {

        }));

        // Height changer
        setButton(new Button(asSlot(1, 8), () -> new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                .setName("&a+")
                .build()).onClick(player -> {

        }));
        setButton(new Button(asSlot(2, 8), () -> new ItemBuilder(Material.SCAFFOLDING)
                .setName("&a" + LE.getText("menu.temp.scanning.height.title"))
                .addDescription(LE.getText("menu.temp.scanning.height.description"))
                .addLore("")
                .addLore("Currently: " + height)
                .build()).onClick(player -> {

        }));
        setButton(new Button(asSlot(3, 8), () -> new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setName("&c-")
                .build()).onClick(player -> {

        }));
    }
}
