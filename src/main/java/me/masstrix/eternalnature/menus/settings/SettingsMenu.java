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
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.core.item.SkullIndex;
import me.masstrix.eternalnature.menus.Button;
import me.masstrix.eternalnature.menus.GlobalMenu;
import me.masstrix.eternalnature.menus.MenuManager;
import me.masstrix.eternalnature.menus.Menus;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.Lang;
import me.masstrix.lang.langEngine.LanguageEngine;
import me.masstrix.version.checker.VersionCheckInfo;
import me.masstrix.version.checker.VersionState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionType;

public class SettingsMenu extends GlobalMenu {

    private final EternalNature PLUGIN;
    private final MenuManager MANAGER;
    private final Configuration CONFIG;
    private final LanguageEngine LANG;
    private VersionCheckInfo versionInfo;

    //
    // Configuration
    //
    private boolean checkUpdates;
    private boolean notifyUpdates;
    private boolean hydEnabled;
    private boolean tmpEnabled;
    private boolean fallingLeavesEnabled;
    private boolean randomSpreadEnabled;
    private boolean ageItemsEnabled;
    private boolean autoPlantEnabled;
    private boolean replantCropsEnabled;

    public SettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.SETTINGS, 5);
        this.CONFIG = plugin.getRootConfig();
        this.MANAGER = menuManager;
        this.LANG = plugin.getLanguageEngine();
        this.PLUGIN = plugin;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        checkUpdates = section.getBoolean("general.check-for-updates");
        notifyUpdates = section.getBoolean("general.notify-update-join");
        hydEnabled = section.getBoolean("hydration.enabled");
        tmpEnabled = section.getBoolean("temperature.enabled");
        fallingLeavesEnabled = section.getBoolean("global.falling-leaves.enabled");
        randomSpreadEnabled = section.getBoolean("global.randomly-spread-trees.enabled");
        ageItemsEnabled = section.getBoolean("global.age-items");
        autoPlantEnabled = section.getBoolean("global.auto-plant.enabled");
        replantCropsEnabled = section.getBoolean("global.auto-plant.replant-crops");
        build();
    }

    @Override
    public String getTitle() {
        return LANG.getText("menu.settings.title");
    }

    @Override
    public void build() {
        versionInfo = PLUGIN.getVersionInfo();

        setButton(new Button(asSlot(0, 4), () -> {
            String version = PLUGIN.getDescription().getVersion();
            String latest = versionInfo == null ? "&8Failed to Check" : versionInfo.getLatest().getName();

            if (!checkUpdates) {
                latest = "&8Checking Disabled";
            }

            if (versionInfo != null) {
                VersionState state = versionInfo.getState();
                switch (state) {
                    case AHEAD: {
                        version = "&6" + version + " (Dev Build)";
                        break;
                    }
                    case CURRENT: {
                        version = "&a" + version + " (Latest)";
                        break;
                    }
                    case BEHIND: {
                        version = "&e" + version + " (Outdated)";
                        break;
                    }
                    case UNKNOWN: {
                        version = "&7" + version;
                        break;
                    }
                }
            }

            return new ItemBuilder(Material.FERN)
                    .setName("&2Eternal Nature")
                    .addDescription("A plugin to make the world be more lively.")
                    .addLore("Developed by &f" + StringUtil.fromStringArray(PLUGIN.getDescription().getAuthors(), ", "),
                            "Version: " + version,
                            "Latest: &7" + latest)
                    .addLore("", "&eClick to get help", "&elinks to the project.")
                    .build();
        }).onClick(player -> {
            player.closeInventory();
            ComponentBuilder builder = new ComponentBuilder("");
            builder.append("\n    [Eternal Nature]\n").color(ChatColor.GREEN);
            builder.append("    Download Page ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("VISIT").color(ChatColor.GREEN).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("\u00A7eClick to visit the plugins download page\non spigotmc.org.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.PLUGIN_PAGE));
            builder.append("\n");
            builder.append("    Official Discord ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("JOIN").color(ChatColor.GREEN).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("\u00A7eClick to join the official discord.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.DISCORD));
            builder.append("\n");
            builder.append("    Plugin Wiki ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("VISIT").color(ChatColor.GREEN).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("\u00A7eClick to get help on the plugins wiki.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.WIKI_PAGE));
            builder.append("\n");
            builder.append("    Submit a bug report ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("HERE").color(ChatColor.GREEN).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text("\u00A7eClick to submit a issue/bug report.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.ISSUES_PAGE));
            builder.append("\n");

            player.spigot().sendMessage(builder.create());
        }));

        LanguageEngine engine = PLUGIN.getLanguageEngine();

        setButton(new Button(asSlot(1, 8), () -> {
            ItemBuilder builder = new ItemBuilder(SkullIndex.PLANET_EARTH)
                    .setName("&a" + LANG.getText("menu.language.title"))
                    .addDescription(LANG.getText("menu.language.description"));

            for (Lang lang : engine.list()) {
                boolean selected = engine.isActive(lang);
                if (selected)
                    builder.addLore(" &a• " + lang.getNiceName());
                else
                    builder.addLore(" &7• " + lang.getNiceName());
            }
            builder.addLore("", "&e" + LANG.getText("menu.common.select"));
            return builder.build();
        }).onClick(player -> {
            MANAGER.getMenu(Menus.LANG_SETTINGS).open(player);
        }));

        setButton(new Button(asSlot(1, 2), () -> new ItemBuilder(Material.POTION)
                .setPotionType(PotionType.SPEED)
                .setName("&a" + LANG.getText("menu.hydration.title"))
                .addDescription(LANG.getText("menu.hydration.description"))
                .addSwitchView("Currently:", hydEnabled)
                .addLore("", "&e" + LANG.getText("menu.common.edit"))
                .build()).onClick(player -> {
            MANAGER.getMenu(Menus.HYDRATION_SETTINGS).open(player);
        }));

        setButton(new Button(asSlot(1, 4), () -> new ItemBuilder(Material.COMPARATOR)
                .setName("&a" + LANG.getText("menu.other.title"))
                .addLore("",
                        (notifyUpdates ? "&a" : "&c") + " ▪&7 Update Notifications",
                        (checkUpdates ? "&a" : "&c") + " ▪&7 Update Checking",
                        "",
                        "&e" + LANG.getText("menu.common.edit"))
                .build()).onClick(player -> {
            MANAGER.getMenu(Menus.OTHER_SETTINGS).open(player);
        }));

        setButton(new Button(asSlot(1, 6), () -> new ItemBuilder(Material.CAMPFIRE)
                .setName("&a" + LANG.getText("menu.temp.title"))
                .addDescription(LANG.getText("menu.temp.description"))
                .addSwitchView("Currently:", tmpEnabled)
                .addLore("", "&e" + LANG.getText("menu.common.edit"))
                .build()).onClick(player -> {
            MANAGER.getMenu(Menus.TEMP_SETTINGS).open(player);
        }));

        setButton(new Button(asSlot(3, 2), () -> new ItemBuilder(Material.KELP)
                .setName("&a" + LANG.getText("menu.leaf-particles.title"))
                .addDescription(LANG.getText("menu.leaf-particles.description"))
                .addSwitchView("Currently:", fallingLeavesEnabled)
                .addLore("&e" + LANG.getText("menu.common.edit"))
                .build())
                .setToggle(LANG.getText("menu.leaf-particles.title"),
                        () -> fallingLeavesEnabled)
                .onClick(player -> {
                    openMenu(MANAGER, Menus.LEAF_PARTICLE_SETTINGS, player);
                }));
        setButton(new Button(asSlot(3, 3), () -> new ItemBuilder(Material.OAK_SAPLING)
                .setName("&a" + LANG.getText("menu.settings.item.auto-plant.title"))
                .addDescription(LANG.getText("menu.settings.item.auto-plant.description"))
                .addSwitch("Currently:", autoPlantEnabled)
                .build()).setToggle(LANG.getText("menu.settings.item.auto-plant.title"),
                () -> autoPlantEnabled)
                .onClick(player -> {
                    CONFIG.set("global.auto-plant.enabled", !autoPlantEnabled);
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(3, 4), () -> new ItemBuilder(Material.WOODEN_HOE)
                .setName("&a" + LANG.getText("menu.settings.item.replant.title"))
                .addDescription(LANG.getText("menu.settings.item.replant.description"))
                .addSwitch("Currently:", replantCropsEnabled)
                .build()).setToggle(LANG.getText("menu.settings.item.replant.title"),
                () -> replantCropsEnabled)
                .onClick(player -> {
                    CONFIG.toggle("global.auto-plant.replant-crops");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(3, 5), () -> new ItemBuilder(Material.OAK_LEAVES)
                .setName("&a" + LANG.getText("menu.settings.item.tree-spread.title"))
                .addDescription(LANG.getText("menu.settings.item.tree-spread.description"))
                .addSwitch("Currently:", randomSpreadEnabled)
                .build()).setToggle(LANG.getText("menu.settings.item.tree-spread.title"),
                () -> randomSpreadEnabled)
                .onClick(player -> {
                    CONFIG.toggle("global.randomly-spread-trees");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(3, 6), () -> new ItemBuilder(Material.ROTTEN_FLESH)
                .setName("&a" + LANG.getText("menu.settings.item.item-aging.title"))
                .addDescription(LANG.getText("menu.settings.item.item-aging.description"))
                .addSwitch("Currently:", ageItemsEnabled)
                .build()).setToggle(LANG.getText("menu.settings.item.item-aging.title"),
                () -> ageItemsEnabled)
                .onClick(player -> {
                    CONFIG.toggle("global.age-items");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(3, 8), () -> new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("&a" + LANG.getText("menu.settings.item.reset-config.title"))
                .addDescription(LANG.getText("menu.settings.item.reset-config.description"))
                .addLore("&e" + LANG.getText("menu.common.reset"))
                .build())
                .onClick(player -> {
                    PLUGIN.saveResource("temperature-config.yml", true);
                    PLUGIN.saveResource("config.yml", true);
                    PLUGIN.getEngine().getDefaultTempProfile().reload();
                    CONFIG.reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    player.closeInventory();
                    player.sendMessage(StringUtil.color(PluginData.PREFIX + "&aReset Config back to default."));
                }));

        setButton(new Button(asSlot(4, 8), () -> new ItemBuilder(Material.BOOK)
                .setName("&a" + LANG.getText("menu.settings.item.reload-config.title"))
                .addDescription(LANG.getText("menu.settings.item.reload-config.description"))
                .addLore("&e" + LANG.getText("menu.common.reload"))
                .build())
                .onClick(player -> {
                    CONFIG.reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    player.closeInventory();
                    player.sendMessage(StringUtil.color(PluginData.PREFIX + "&aReloaded Config."));
                }));
    }
}
