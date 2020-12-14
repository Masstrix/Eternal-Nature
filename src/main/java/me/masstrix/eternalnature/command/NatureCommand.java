/*
 * Copyright 2019 Matthew Denton
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

package me.masstrix.eternalnature.command;

import me.masstrix.eternalnature.EternalEngine;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.config.ConfigPath;
import me.masstrix.eternalnature.core.temperature.TempModifierType;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.temperature.modifier.BiomeModifier;
import me.masstrix.eternalnature.core.world.LeafEmitter;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.menus.Menus;
import me.masstrix.eternalnature.player.UserData;
import me.masstrix.eternalnature.util.BuildInfo;
import me.masstrix.version.checker.VersionCheckInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NatureCommand extends EternalCommand {

    private static final String WORLD_NOT_LOADED = PluginData.PREFIX + "&cNo world is loaded with that name.";

    private EternalNature plugin;

    public NatureCommand(EternalNature plugin) {
        super("eternalnature");
        this.plugin = plugin;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            msg("");
            msg("     &2&lEternal Nature");
            msg("     &7&oby Masstrix");
            msg("");
            msg("&a/eternal reload &7- Reloads all config files.");
            msg("&a/eternal world <world> &7- Provides options for a world.");
            msg("&a/eternal reloadWorld <world> &7- Reloads data for a world.");
            msg("&a/eternal resetConfig &7- Resets all config files.");
            msg("&a/eternal stats &7- Shows background stats.");
            msg("&a/eternal version &7- View version and update info.");
            msg("&a/eternal setting &7- Opens a GUI to edit settings.");
            msg("&a/hydrate <user> &7- Hydrates a user to max.");
            msg("");
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            msg(PluginData.PREFIX + "&7Reloading files...");
            plugin.getRootConfig().reload();
            msg(PluginData.PREFIX + "&aReloaded config files");
        }

        else if (args[0].equalsIgnoreCase("world")) {
            String world = args.length > 1 ? args[1] : "<world>";
            if (args.length == 1) {
                msg("");
                msg("     &2&lEternal Nature");
                msg("     &7&o/world options");
                msg("");
                msg("&a/eternal world list &7- Lists all loaded worlds.");
                msg("&a/eternal world " + world + " info &7- Displays info about that world.");
                msg("&a/eternal world " + world + " makeCustomConfig &7- " +
                        "Makes a custom temperature config for world specific configuration.");
                msg("");
                return;
            }

            WorldProvider provider = plugin.getEngine().getWorldProvider();
            String sub = args[1];

            if (sub.equalsIgnoreCase("list") && args.length == 2) {
                List<String> worlds = provider.getWorldNames();
                msg("");
                msg("     &2&lLoaded Worlds");
                worlds.forEach(name -> msg(" &2• &f" + name));
                msg("");
                return;
            }

            // Stop if command does not have arguments
            if (args.length < 3) {
                msg(PluginData.PREFIX + "&cInvalid use. For help use /eternal world");
                return;
            }

            sub = args[2];

            // Handle creating a custom config for world. This bypasses
            // the need for a world to be loaded and lets the user
            // define any name for the world being created.
            if (sub.equalsIgnoreCase("makeCustomConfig")) {
                WorldData data = provider.getWorld(Bukkit.getWorld(world));
                if (data == null) {
                    msg(WORLD_NOT_LOADED);
                    return;
                }
                msg(PluginData.PREFIX + "&7Creating custom config for world " + world + "...");
                boolean success = data.createCustomTemperatureConfig();
                if (success)
                    msg(PluginData.PREFIX + "&aCreated custom config for world &e" + world + "&a.");
                else msg(PluginData.PREFIX + "&7World &e" + world + "&7 already had a custom config.");
                return;
            }

            // Stop if the world does not exist
            World bukkitWorld = Bukkit.getWorld(world);
            if (bukkitWorld == null) {
                msg(WORLD_NOT_LOADED);
                return;
            }

            if (!provider.isLoaded(bukkitWorld)) {
                msg(PluginData.PREFIX + "&cWorld does not have any loaded data.");
                return;
            }

            // Handle sub commands for /eternal world
            if (sub.equalsIgnoreCase("info")) {
                WorldData data = provider.getWorld(bukkitWorld);
                TemperatureProfile t = data.getTemperatures();
                msg("");
                msg("     &2&lEternal Nature");
                msg("     &6&o" + world + "&6's info");
                msg("");
                if (wasPlayer()) {
                    Player player = (Player) getSender();

                    if (data.asBukkit() == player.getWorld()) {
                        Block standing = player.getLocation().getBlock();
                        BiomeModifier mod = (BiomeModifier) t.getModifier(TempModifierType.BIOME, standing.getBiome());
                        if (mod == null) {
                            msg("Current Biome: &7Unknown");
                        } else {
                            msg("Current Biome:: &7" + mod.getName()
                                    + " (&f" + mod.getEmission() + "&7)");
                        }
                    } else {
                        msg("&7&oYou are not in this world.");
                    }
                }
                msg("Hottest record: &c" + t.getMaxTemp());
                msg("Coldest record: &b" + t.getMinTemp());
                msg("");
            }
            else {
                msg(PluginData.PREFIX + "&cInvalid use. For help use /eternal world");
            }
        }

        else if (args[0].equalsIgnoreCase("resetConfig")) {
            msg(PluginData.PREFIX + "&7Resetting files...");
            plugin.saveResource("temperature-config.yml", true);
            plugin.saveResource("config.yml", true);
            plugin.getEngine().getDefaultTempProfile().reload();
            plugin.getRootConfig().reload();
            msg(PluginData.PREFIX + "&aReset config files back to default");
        }

        else if (args[0].equalsIgnoreCase("settings")) {
            if (wasPlayer()) {
                plugin.getEngine().getMenuManager()
                        .getMenu(Menus.SETTINGS.getId()).open((Player) getSender());
            } else {
                msg("Settings can only be accessed in game.");
            }
        }

        else if (args[0].equalsIgnoreCase("stats")
                || args[0].equalsIgnoreCase("info")) {
            EternalEngine engine = plugin.getEngine();
            LeafEmitter leafEmitter = (LeafEmitter) engine.getWorker(LeafEmitter.class);

            msg("");
            msg("     &2&lEternal Nature");
            msg("     &7Background Stats");
            msg("");
            msg("Players Cached: &7" + engine.getCashedUsers().size());
            msg("Worlds Loaded: &7" + engine.getWorldProvider().getLoaded());
            msg("Leaf Particles: &a" + leafEmitter.getParticleCount() + "&7/" + leafEmitter.getMaxParticles());
            msg("");
        }

        else if (args[0].equalsIgnoreCase("version")) {
            msg("");
            msg("     &e&lEternal Nature");
            msg("     &7Version Info");
            msg("");
            msg(" Build: &7" + BuildInfo.getBuild());
            msg(" Current Version: &7" + BuildInfo.getVersion());
            if (BuildInfo.isSnapshot()) {
                msg("   &6&oThis version is a snapshot.");
            }
            if (plugin.getVersionInfo() == null) {
                if (plugin.getRootConfig().getYml().getBoolean(ConfigPath.UPDATE_CHECK)) {
                    msg("&cUnable to check plugin version.");
                } else {
                    msg("&cVersion checking is disabled.");
                }
            } else {
                VersionCheckInfo info = plugin.getVersionInfo();
                switch (info.getState()) {
                    case UNKNOWN: {
                        msg("&cError trying to check version.");
                        break;
                    }
                    case BEHIND: {
                        msg(" Latest: &7" + info.getLatest().getName() + " &6(update available)");

                        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/natural-environment.43290/history");
                        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {
                                new TextComponent("\u00A7eClick to view latest release.\n\u00A77Redirects to spigot.org")
                        });

                        TextComponent text = new TextComponent(" ");
                        TextComponent infoTxt = new TextComponent("CLICK HERE TO UPDATE");
                        infoTxt.setBold(true);
                        infoTxt.setColor(ChatColor.GOLD);
                        infoTxt.setClickEvent(click);
                        infoTxt.setHoverEvent(hover);

                        if (wasPlayer()) {
                            ((Player) getSender()).spigot().sendMessage(text, infoTxt);
                        }
                        break;
                    }
                    case AHEAD: {
                        msg("&6This version is ahead of the latest known currently. This might be because it's a recent release.");
                        break;
                    }
                    case CURRENT: {
                        msg("&a Plugin is up to date.");
                    }
                }
            }
            msg("");
        }

        else if (args[0].equalsIgnoreCase("debug") && wasPlayer()) {
            UserData data = plugin.getEngine().getUserData(((Player) getSender()).getUniqueId());
            data.setDebug(!data.isDebugEnabled());
            if (data.isDebugEnabled()) {
                msg(PluginData.PREFIX + "&7Enabled &6debug mode.");
            } else {
                msg(PluginData.PREFIX + "&7Disabled &6debug mode.");
            }
        }
    }

    @Override
    public List<String> tabComplete(String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "world", "stats",
                    "version", "settings", "resetConfig", "fixLeafEffect");
        }
        else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("world")) {
                List<String> names = plugin.getEngine().getWorldProvider().getWorldNames();

                if (args.length == 2) {
                    List<String> worlds = new ArrayList<>(names);
                    worlds.add("list");
                    return worlds;
                }

                if (args.length == 3 && names.contains(args[1])) {
                    return Arrays.asList("reload", "makeCustomConfig", "info");
                }

                if (args.length == 3) {
                    return Collections.singletonList("makeCustomConfig");
                }
            }
        }
        return super.tabComplete(args);
    }
}
