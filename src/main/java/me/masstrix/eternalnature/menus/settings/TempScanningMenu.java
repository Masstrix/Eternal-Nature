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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Configurable.Path("temperature.scanning")
public class TempScanningMenu extends GlobalMenu {

    private final EternalNature PLUGIN;
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
        this.PLUGIN = plugin;
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
        useBiomes = section.getBoolean("use-biomes", true);
        useWeather = section.getBoolean("use-weather", true);
        useItems = section.getBoolean("use-items", true);
        useEnvironment = section.getBoolean("use-environment", true);
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
                .setName("&a+1")
                .addLore(String.valueOf(area))
                .build()).onClick(player -> {
                    area++;
            CONFIG.set("temperature.scanning.advanced.area", area);
            CONFIG.save().reload();
            playVolumeChange(player, 2, 3, Sound.BLOCK_NOTE_BLOCK_PLING, true);
        }));
        setButton(new Button(asSlot(2, 7), () -> new ItemBuilder(Material.SCAFFOLDING)
                .setName("&a" + LE.getText("menu.temp.scanning.area.title"))
                .addDescription(LE.getText("menu.temp.scanning.area.description"))
                .addLore("Currently: &a" + area)
                .build()));
        setButton(new Button(asSlot(3, 7), () -> new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setName("&c-1")
                .addLore(String.valueOf(area))
                .build()).onClick(player -> {
            area--;
            CONFIG.set("temperature.scanning.advanced.area", area);
            CONFIG.save().reload();
            playVolumeChange(player, 2, 3, Sound.BLOCK_NOTE_BLOCK_PLING, false);
        }));

        // Height changer
        setButton(new Button(asSlot(1, 8), () -> new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                .setName("&a+1")
                .addLore(String.valueOf(height))
                .build()).onClick(player -> {
            height++;
            CONFIG.set("temperature.scanning.advanced.height", height);
            CONFIG.save().reload();
            playVolumeChange(player, 2, 3, Sound.BLOCK_NOTE_BLOCK_PLING, true);
        }));
        setButton(new Button(asSlot(2, 8), () -> new ItemBuilder(Material.SCAFFOLDING)
                .setName("&a" + LE.getText("menu.temp.scanning.height.title"))
                .addDescription(LE.getText("menu.temp.scanning.height.description"))
                .addLore("Currently: &a" + height)
                .build()));
        setButton(new Button(asSlot(3, 8), () -> new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setName("&c-1")
                .addLore(String.valueOf(height))
                .build()).onClick(player -> {
            height--;
            CONFIG.set("temperature.scanning.advanced.height", height);
            CONFIG.save().reload();
            playVolumeChange(player, 2, 3, Sound.BLOCK_NOTE_BLOCK_PLING, false);
        }));
    }

    private void playVolumeChange(Player player, int times, int delay, Sound sound, boolean up) {
        float pitch = (1F / times) * 0.5F;
        new BukkitRunnable() {
            int it = 0;
            float p = up ? 0.5F : 1F;
            @Override
            public void run() {
                player.playSound(player.getLocation(), sound, 1, p);
                if (up) p += pitch;
                else p -= pitch;

                if (it++ == times) {
                    this.cancel();
                }
            }
        }.runTaskTimer(PLUGIN, 0, delay);
    }
}
