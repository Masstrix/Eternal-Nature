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
import me.masstrix.eternalnature.player.DebugOptions;
import me.masstrix.eternalnature.player.UserData;
import me.masstrix.eternalnature.util.BuildInfo;
import me.masstrix.version.checker.VersionCheckInfo;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NatureCommand extends EternalCommand {

    private static final String WORLD_NOT_LOADED = PluginData.PREFIX + "&cNo world is loaded with that name.";
    private TestCommand testCommand = new TestCommand();

    private EternalNature plugin;

    public NatureCommand(EternalNature plugin) {
        super("eternalnature");
        this.plugin = plugin;
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            msg("");
            msg(PluginData.Colors.PRIMARY   + "     &lEternal Nature");
            msg(PluginData.Colors.TERTIARY + "     &oby Masstrix");
            msg("");
            msg(new PluginData.Colors().secondary("/eternal settings")
                    .message(" - Opens a GUI to change settings."));
            msg(new PluginData.Colors().secondary("/eternal reload")
                    .message(" - Reloads all config files"));
            msg(new PluginData.Colors().secondary("/eternal world <world>")
                    .message(" - Provides options for a world."));
            msg(new PluginData.Colors().secondary("/eternal reloadWorld <wold>")
                    .message(" - Reloads data for a world."));
            msg(new PluginData.Colors().secondary("/eternal resetConfig")
                    .message(" - Resets all config files."));
            msg(new PluginData.Colors().secondary("/eternal stats")
                    .message(" - Shows background running stats."));
            msg(new PluginData.Colors().secondary("/eternal version")
                    .message(" - Check the current plugin version."));
            msg(new PluginData.Colors().secondary("/eternal reloadTriggers")
                    .message(" - Reloads the triggers config."));
            msg(new PluginData.Colors().secondary("/eternal summon <entity>")
                    .message(" - Summons an custom entity type from the plugin."));
            msg(new PluginData.Colors().secondary("/hydrate <player>")
                    .message(" - Hydrates a player."));
            msg("");
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            msg(PluginData.PREFIX + "Reloading files...");
            plugin.getRootConfig().reload();
            msg(PluginData.PREFIX + "Reloaded config files");
        }

        else if (args[0].equalsIgnoreCase("reloadTriggers")) {
            msg(PluginData.PREFIX + "Reloading Triggers...");
            plugin.getTriggerManager().load();
            msg(PluginData.PREFIX + "Reloaded triggers.");
        }

        else if (args[0].equalsIgnoreCase("world")) {
            String world = args.length > 1 ? args[1] : "<world>";
            if (args.length == 1) {
                msg("");
                msg(PluginData.Colors.PRIMARY   + "     &lEternal Nature");
                msg(PluginData.Colors.TERTIARY + "     &o/world options");
                msg("");
                msg(new PluginData.Colors().secondary("/eternal world list")
                        .message(" - Lists all loaded world configs."));
                msg(new PluginData.Colors().secondary("/eternal world " + world + " info")
                        .message(" - Displays info about that world."));
                msg(new PluginData.Colors().secondary("/eternal world " + world + " makeCustomConfig")
                        .message(" - Makes a custom temperature config for world specific configurations"));
                msg("");
                return;
            }

            WorldProvider provider = plugin.getEngine().getWorldProvider();
            String sub = args[1];

            if (sub.equalsIgnoreCase("list") && args.length == 2) {
                List<String> worlds = provider.getWorldNames();
                msg("");
                msg(PluginData.Colors.PRIMARY   + "     &lEternal Nature");
                msg(PluginData.Colors.SECONDARY + "     &oAll Loaded worlds");
                worlds.forEach(name -> msg(" &2• &f" + name));
                msg("");
                return;
            }

            // Stop if command does not have arguments
            if (args.length < 3) {
                msg(PluginData.PREFIX + PluginData.Colors.ERROR + "Invalid use. For help use /eternal world");
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
                msg(PluginData.PREFIX + "Creating custom config for world " + world + "...");
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
                msg(PluginData.PREFIX + PluginData.Colors.ERROR +  "World does not have any loaded data.");
                return;
            }

            // Handle sub commands for /eternal world
            if (sub.equalsIgnoreCase("info")) {
                WorldData data = provider.getWorld(bukkitWorld);
                TemperatureProfile t = data.getTemperatures();
                msg("");
                msg(PluginData.Colors.PRIMARY   + "     &lEternal Nature");
                msg(PluginData.Colors.SECONDARY + "     &o" + world + "'s info");
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
            msg(PluginData.Colors.PRIMARY   + "     &lEternal Nature");
            msg(PluginData.Colors.TERTIARY + "     &oBackground stats");
            msg("");
            msg("Players Cached: &7" + engine.getCashedUsers().size());
            msg("Worlds Loaded: &7" + engine.getWorldProvider().getLoaded());
            msg("Leaf Particles: &a" + leafEmitter.getParticleCount() + "&7/" + leafEmitter.getMaxParticles());
            msg("");
        }

        else if (args[0].equalsIgnoreCase("version")) {
            msg("");
            msg(PluginData.Colors.PRIMARY   + "     &lEternal Nature");
            msg(PluginData.Colors.TERTIARY + "     &o" + BuildInfo.getBuildKind());
            msg("");
            msg(" Current Version: &7" + BuildInfo.getVersion() + " &o&8(" + BuildInfo.getBuild() + ")");
            if (plugin.getVersionInfo() == null) {
                if (plugin.getRootConfig().getYml().getBoolean(ConfigPath.UPDATE_CHECK)) {
                    msg("&cUnable to check plugin version.");
                } else {
                    msg("&cVersion checking is disabled.");
                }
            } else {
                VersionCheckInfo info = plugin.getVersionInfo();
                switch (info.getState()) {
                    case UNKNOWN -> {
                        msg("&cError trying to check version.");
                    }
                    case BEHIND -> {
                        msg(" Latest: &7" + info.getLatest().getName() + " &6(update available)");

                        ClickEvent click = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/natural-environment.43290/history");
                        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
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
                    }
                    case AHEAD -> {
                        msg("&6This version is ahead of the latest known currently. This might be because it's a recent release.");
                    }
                    case CURRENT -> {
                        msg("&a Plugin is up to date.");
                    }
                }
            }
            msg("");
        }

        else if (args[0].equalsIgnoreCase("debug") && wasPlayer()) {
            if (args.length == 1) {
                msg("");
                msg(PluginData.Colors.PRIMARY   + "     &lEternal Nature");
                msg(PluginData.Colors.TERTIARY + "     &oDebug options");
                msg("");
                msg(new PluginData.Colors().secondary("/eternal debug toggle")
                        .message(" - Toggles debug mode on or off"));
                msg(new PluginData.Colors().secondary("/eternal debug set <option>")
                        .message(" - Sets an option for the debug info."));
                msg(new PluginData.Colors().secondary("/eternal debug options")
                        .message(" - Lists all debug options."));
                return;
            }

            if (args[1].equalsIgnoreCase("toggle")) {
                UserData data = getData();
                data.setDebug(!data.isDebugEnabled());
                if (data.isDebugEnabled()) {
                    msg(PluginData.PREFIX + "Enabled &6debug mode.");
                } else {
                    msg(PluginData.PREFIX + "Disabled &6debug mode.");
                }
            }

            else if (args[1].equalsIgnoreCase("options")) {
                UserData data = getData();
                sendDebugOptionsList(data);
            }

            else if(args[1].equalsIgnoreCase("set")) {
                boolean undefined = args.length == 2;
                DebugOptions.Type type = undefined ? null : DebugOptions.Type.find(args[2]);

                UserData data = getData();

                // Show a list and error for not giving a valid option name
                if (type == null) {
                    String error = undefined ? "Please define a option to set" : "No option name matches " + args[2];
                    msg(PluginData.PREFIX + "&c" + error);
                    sendDebugOptionsList(data);
                    return;
                }

                // show what the option is currently set as
                if (args.length == 3) {
                    msg(" Current value for " + type.getSimpleName() + ": " + type.isEnabled(data));
                }
                // change the option to the new value
                else {
                    // get new value
                    String arg = args[3];
                    boolean newVal = arg.equalsIgnoreCase("true") || arg.equals("1");
                    if (arg.equalsIgnoreCase("toggle"))
                        newVal = !type.isEnabled(data);
                    type.set(data, newVal);
                    data.save();
                    msg(" Set " + type.getSimpleName() + " to " + newVal);
                }
            }
        }

        else if (args[0].equalsIgnoreCase("test") && wasPlayer()) {
            testCommand.execute((Player) getSender(), args);
        }

        else if (args[0].equalsIgnoreCase("summon") && wasPlayer()) {
            if (args.length == 1) {
                return;
            }

            String entityType = args[1].toLowerCase();
            Location loc = ((Player) getSender()).getLocation();

            if ("leaf".equals(entityType)) {
                LeafEmitter leafEmitter = (LeafEmitter) plugin.getEngine().getWorker(LeafEmitter.class);
                leafEmitter.spawn(loc);
            }
        }
    }

    private void sendDebugOptionsList(UserData data) {
        msg(" &7Here is a list of all the options:");
        msg(" &7&o(You can click to toggle them off or on)");

        // Show all options that can be set with clickable toggles
        for (DebugOptions.Type option : DebugOptions.Type.values()) {
            sendOptionToggle(data, option);
        }
    }

    private void sendOptionToggle(UserData data, DebugOptions.Type option) {
        boolean enabled = option.isEnabled(data);
        ClickEvent click = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/en debug set " + option.name() + " toggle");
        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("\u00A7eClick to toggle"));

        TextComponent text = new TextComponent(" • ");
        TextComponent infoTxt = new TextComponent(option.getSimpleName());
        infoTxt.setColor(enabled ? ChatColor.GREEN : ChatColor.RED);
        infoTxt.setClickEvent(click);
        infoTxt.setHoverEvent(hover);

        ((Player) getSender()).spigot().sendMessage(text, infoTxt);
    }

    private UserData getData() {
        return plugin.getEngine().getUserData(((Player) getSender()).getUniqueId());
    }

    @Override
    public List<String> tabComplete(String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "world", "stats",
                    "version", "settings", "resetConfig",
                    "fixLeafEffect", "reloadTriggers", "debug", "summon");
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
            else if (args[0].equalsIgnoreCase("debug")) {
                if (args.length == 2) {
                    return Arrays.asList("toggle", "set", "options");
                }
                if (args[1].equalsIgnoreCase("set")) {
                    if (args.length == 4) return Arrays.asList("true", "false");
                    return DebugOptions.Type.getNames();
                }
            }
            else if(args[0].equalsIgnoreCase("summon")) {
                if (args.length == 2) {
                    return Arrays.asList("leaf");
                }
            }
        }
        return super.tabComplete(args);
    }
}
