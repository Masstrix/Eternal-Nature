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
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.eternalnature.util.VersionChecker;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionType;

public class SettingsMenu extends GlobalMenu {

    private EternalNature plugin;
    private VersionChecker.VersionMeta versionMeta;

    public SettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.SETTINGS, "Eternal Nature Settings", 5);
        SystemConfig config = plugin.getSystemConfig();
        this.plugin = plugin;

        versionMeta = plugin.getVersionMeta();

        setButton(new Button(getInventory(), asSlot(0, 4), () -> {

            String version = plugin.getDescription().getVersion();
            String latest = versionMeta == null ? "&8Unknown" : versionMeta.getLatestVersion();

            if (!plugin.getSystemConfig().isEnabled(ConfigOption.UPDATES_CHECK)) {
                latest = "&8Checking Disabled";
            }

            if (versionMeta != null) {
                VersionChecker.PluginVersionState state = versionMeta.getState();
                switch (state) {
                    case DEV_BUILD: {
                        version = "&6" + version + " (Dev Build)";
                        break;
                    }
                    case LATEST: {
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
                    .addLore("Improving the survival experience.",
                            "",
                            "Developed by &f" + StringUtil.fromStringArray(plugin.getDescription().getAuthors(), ", "),
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

        setButton(new Button(getInventory(), asSlot(1, 2), () -> new ItemBuilder(Material.POTION)
                .setPotionType(PotionType.SPEED)
                .setName("&aHydration Settings")
                .addLore("", "Change the settings for how", "hydration works.", "")
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.HYDRATION_ENABLED))
                .addLore("", "&eClick to view & edit")
                .build()).onClick(player -> {
            menuManager.getMenu(Menus.HYDRATION_SETTINGS).open(player);
        }));

        setButton(new Button(getInventory(), asSlot(1, 4), () -> new ItemBuilder(Material.COMPARATOR)
                .setName("&aOther Settings")
                .addLore("",
                        (config.isEnabled(ConfigOption.UPDATES_NOTIFY) ? "&a" : "&c") + " ▪&7 Update Notifications",
                        (config.isEnabled(ConfigOption.UPDATES_CHECK) ? "&a" : "&c") + " ▪&7 Update Checking",
                        "",
                        "&eClick to view & edit")
                .build()).onClick(player -> {
            menuManager.getMenu(Menus.OTHER_SETTINGS).open(player);
        }));

        setButton(new Button(getInventory(), asSlot(1, 6), () -> new ItemBuilder(Material.CAMPFIRE)
                .setName("&aTemperature Settings")
                .addLore("", "Change the settings for how", "temperature works.", "")
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_ENABLED))
                .addLore("", "&eClick to view & edit")
                .build()).onClick(player -> {
            menuManager.getMenu(Menus.TEMP_SETTINGS).open(player);
        }));

        setButton(new Button(getInventory(), asSlot(3, 2), () -> new ItemBuilder(Material.KELP)
                .setName("&aFalling Leaves")
                .addLore("", "Set if leave blocks will emmit", "a leaf particle randomly.", "")
                .addSwitchView("Currently:", config.isEnabled(ConfigOption.LEAF_EFFECT))
                .addLore("&eClick to view & edit")
                .build())
                .setToggle("Falling Leaves", () -> config.isEnabled(ConfigOption.LEAF_EFFECT))
                .onClick(player -> {
                    openMenu(menuManager, Menus.LEAF_PARTICLE_SETTINGS, player);
                }));
        setButton(new Button(getInventory(), asSlot(3, 3), () -> new ItemBuilder(Material.OAK_SAPLING)
                .setName("&aAuto Plant")
                .addLore("", "Set if foliage have a chance to", "auto plant them self.",
                        "This includes flowers, saplings", "and crops.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.AUTO_PLANT))
                .build()).setToggle("Auto Plant", () -> config.isEnabled(ConfigOption.AUTO_PLANT))
                .onClick(player -> {
                    config.toggle(ConfigOption.AUTO_PLANT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(3, 4), () -> new ItemBuilder(Material.WOODEN_HOE)
                .setName("&aAuto Replant Crops")
                .addLore("", "Set if crops will get", "auto replanted when harvested.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.AUTO_REPLANT))
                .build()).setToggle("Auto Replant", () -> config.isEnabled(ConfigOption.AUTO_REPLANT))
                .onClick(player -> {
                    config.toggle(ConfigOption.AUTO_REPLANT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(3, 5), () -> new ItemBuilder(Material.OAK_LEAVES)
                .setName("&aRandom Tree Spread")
                .addLore("", "Set if trees randomly", "drop saplings making forests", "very slowly grow over time.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.RANDOM_TREE_SPREAD))
                .build()).setToggle("Random Tree Spread", () -> config.isEnabled(ConfigOption.RANDOM_TREE_SPREAD))
                .onClick(player -> {
                    config.toggle(ConfigOption.RANDOM_TREE_SPREAD);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 8), () -> new ItemBuilder(Material.BARRIER)
                .setName("&aReset Config")
                .addLore("", "Resets the config to default settings.", "", "&eClick to Reset Config")
                .build())
                .onClick(player -> {
                    plugin.saveResource("temperature-config.yml", true);
                    plugin.saveResource("config.yml", true);
                    plugin.getEngine().getTemperatureData().loadConfigData();
                    plugin.getSystemConfig().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    player.closeInventory();
                    player.sendMessage(StringUtil.color(PluginData.PREFIX + "&aReset Config back to default."));
                }));

        setButton(new Button(getInventory(), asSlot(2, 8), () -> new ItemBuilder(Material.EMERALD)
                .setName("&aReload Config")
                .addLore("", "Reloads the config files.", "", "&eClick to Reload Config")
                .build())
                .onClick(player -> {
                    config.reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    player.closeInventory();
                    player.sendMessage(StringUtil.color(PluginData.PREFIX + "&aReloaded Config."));
                }));
    }

    @Override
    public void onOpen(Player who) {
        versionMeta = plugin.getVersionMeta();
    }
}
