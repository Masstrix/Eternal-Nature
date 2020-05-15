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
import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.core.item.SkullIndex;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.Lang;
import me.masstrix.lang.langEngine.LanguageEngine;
import me.masstrix.version.checker.VersionCheckInfo;
import me.masstrix.version.checker.VersionState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.potion.PotionType;

public class SettingsMenu extends GlobalMenu {

    private EternalNature plugin;
    private MenuManager menuManager;
    private SystemConfig config;
    private LanguageEngine le;
    private VersionCheckInfo versionInfo;

    public SettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.SETTINGS, 5);
        this.config = plugin.getSystemConfig();
        this.menuManager = menuManager;
        this.le = plugin.getLanguageEngine();
        this.plugin = plugin;
    }

    @Override
    public String getTitle() {
        return le.getText("menu.settings.title");
    }

    @Override
    public void build() {
        versionInfo = plugin.getVersionInfo();

        setButton(new Button(getInventory(), asSlot(0, 4), () -> {

            String version = plugin.getDescription().getVersion();
            String latest = versionInfo == null ? "&8Unknown" : versionInfo.getLatest().getName();

            if (!plugin.getSystemConfig().isEnabled(ConfigOption.UPDATES_CHECK)) {
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
                    .setName("&aEternal Nature")
                    .addDescription("A plugin to make the world be more lively.")
                    .addLore("Developed by &f" + StringUtil.fromStringArray(plugin.getDescription().getAuthors(), ", "),
                            "Version: " + version,
                            "Latest: &7" + latest)
                    .addLore("", "&eClick to get help", "&elinks to the project.")
                    .build();
        }).onClick(player -> {
            player.closeInventory();
            ComponentBuilder builder = new ComponentBuilder("");
            builder.append("\n    [Eternal Nature]\n").color(ChatColor.GREEN);
            builder.append("    Download Page ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("VISIT").color(ChatColor.GOLD).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText("\u00A7eClick to visit the plugins download page\non spigotmc.org.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.PLUGIN_PAGE));
            builder.append("\n");
            builder.append("    Official Discord ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("JOIN").color(ChatColor.AQUA).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText("\u00A7eClick to join the official discord.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.DISCORD));
            builder.append("\n");
            builder.append("    Plugin Wiki ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("VISIT").color(ChatColor.AQUA).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText("\u00A7eClick to get help on the plugins wiki.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.WIKI_PAGE));
            builder.append("\n");
            builder.append("    Submit a bug report ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.WHITE);
            builder.append("HERE").color(ChatColor.AQUA).bold(true).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    TextComponent.fromLegacyText("\u00A7eClick to submit a issue/bug report.")))
                    .event(new ClickEvent(ClickEvent.Action.OPEN_URL, PluginData.ISSUES_PAGE));
            builder.append("\n");

            player.spigot().sendMessage(builder.create());
        }));

        LanguageEngine engine = plugin.getLanguageEngine();

        setButton(new Button(getInventory(), asSlot(1, 8), () -> {
            ItemBuilder builder = new ItemBuilder(SkullIndex.PLANET_EARTH)
                    .setName("&a" + le.getText("menu.language.title"))
                    .addDescription(le.getText("menu.language.description"));

            for (Lang lang : engine.list()) {
                boolean selected = engine.isActive(lang);
                if (selected)
                    builder.addLore(" &a• " + lang.getNiceName());
                else
                    builder.addLore(" &7• " + lang.getNiceName());
            }
            builder.addLore("", "&e" + le.getText("menu.common.select"));
            return builder.build();
        }).onClick(player -> {
            menuManager.getMenu(Menus.LANG_SETTINGS).open(player);
        }));

        setButton(new Button(getInventory(), asSlot(1, 2), () -> new ItemBuilder(Material.POTION)
                .setPotionType(PotionType.SPEED)
                .setName("&a" + le.getText("menu.hydration.title"))
                .addDescription(le.getText("menu.hydration.description"))
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.HYDRATION_ENABLED))
                .addLore("", "&e" + le.getText("menu.common.edit"))
                .build()).onClick(player -> {
            menuManager.getMenu(Menus.HYDRATION_SETTINGS).open(player);
        }));

        setButton(new Button(getInventory(), asSlot(1, 4), () -> new ItemBuilder(Material.COMPARATOR)
                .setName("&a" + le.getText("menu.other.title"))
                .addLore("",
                        (config.isEnabled(ConfigOption.UPDATES_NOTIFY) ? "&a" : "&c") + " ▪&7 Update Notifications",
                        (config.isEnabled(ConfigOption.UPDATES_CHECK) ? "&a" : "&c") + " ▪&7 Update Checking",
                        "",
                        "&e" + le.getText("menu.common.edit"))
                .build()).onClick(player -> {
            menuManager.getMenu(Menus.OTHER_SETTINGS).open(player);
        }));

        setButton(new Button(getInventory(), asSlot(1, 6), () -> new ItemBuilder(Material.CAMPFIRE)
                .setName("&a" + le.getText("menu.temp.title"))
                .addDescription(le.getText("menu.temp.description"))
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_ENABLED))
                .addLore("", "&e" + le.getText("menu.common.edit"))
                .build()).onClick(player -> {
            menuManager.getMenu(Menus.TEMP_SETTINGS).open(player);
        }));

        setButton(new Button(getInventory(), asSlot(3, 2), () -> new ItemBuilder(Material.KELP)
                .setName("&a" + le.getText("menu.leaf-particles.title"))
                .addDescription(le.getText("menu.leaf-particles.description"))
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.LEAF_EFFECT))
                .addLore("&e" + le.getText("menu.common.edit"))
                .build())
                .setToggle(le.getText("menu.leaf-particles.title"),
                        () -> config.isEnabled(ConfigOption.LEAF_EFFECT))
                .onClick(player -> {
                    openMenu(menuManager, Menus.LEAF_PARTICLE_SETTINGS, player);
                }));
        setButton(new Button(getInventory(), asSlot(3, 3), () -> new ItemBuilder(Material.OAK_SAPLING)
                .setName("&a" + le.getText("menu.settings.item.auto-plant.title"))
                .addDescription(le.getText("menu.settings.item.auto-plant.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.AUTO_PLANT))
                .build()).setToggle(le.getText("menu.settings.item.auto-plant.title"),
                () -> config.isEnabled(ConfigOption.AUTO_PLANT))
                .onClick(player -> {
                    config.toggle(ConfigOption.AUTO_PLANT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(3, 4), () -> new ItemBuilder(Material.WOODEN_HOE)
                .setName("&a" + le.getText("menu.settings.item.replant.title"))
                .addDescription(le.getText("menu.settings.item.replant.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.AUTO_REPLANT))
                .build()).setToggle(le.getText("menu.settings.item.replant.title"),
                () -> config.isEnabled(ConfigOption.AUTO_REPLANT))
                .onClick(player -> {
                    config.toggle(ConfigOption.AUTO_REPLANT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(3, 5), () -> new ItemBuilder(Material.OAK_LEAVES)
                .setName("&a" + le.getText("menu.settings.item.tree-spread.title"))
                .addDescription(le.getText("menu.settings.item.tree-spread.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.RANDOM_TREE_SPREAD))
                .build()).setToggle(le.getText("menu.settings.item.tree-spread.title"),
                () -> config.isEnabled(ConfigOption.RANDOM_TREE_SPREAD))
                .onClick(player -> {
                    config.toggle(ConfigOption.RANDOM_TREE_SPREAD);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(3, 8), () -> new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("&a" + le.getText("menu.settings.item.reset-config.title"))
                .addDescription(le.getText("menu.settings.item.reset-config.description"))
                .addLore("&e" + le.getText("menu.common.reset"))
                .build())
                .onClick(player -> {
                    plugin.saveResource("temperature-config.yml", true);
                    plugin.saveResource("config.yml", true);
                    plugin.getEngine().getDefaultTemperatures().loadData();
                    plugin.getSystemConfig().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    player.closeInventory();
                    player.sendMessage(StringUtil.color(PluginData.PREFIX + "&aReset Config back to default."));
                }));

        setButton(new Button(getInventory(), asSlot(4, 8), () -> new ItemBuilder(Material.BOOK)
                .setName("&a" + le.getText("menu.settings.item.reload-config.title"))
                .addDescription(le.getText("menu.settings.item.reload-config.description"))
                .addLore("&e" + le.getText("menu.common.reload"))
                .build())
                .onClick(player -> {
                    config.reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    player.closeInventory();
                    player.sendMessage(StringUtil.color(PluginData.PREFIX + "&aReloaded Config."));
                }));
    }
}
