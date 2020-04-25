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

package me.masstrix.eternalnature;

import me.masstrix.eternalnature.command.*;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.metric.Metrics;
import me.masstrix.eternalnature.listeners.*;
import me.masstrix.eternalnature.util.MinecraftVersion;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.eternalnature.util.VersionChecker;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;

public class EternalNature extends JavaPlugin {

    private static final MinecraftVersion REQUIRED_VER = new MinecraftVersion("1.14");
    private EternalEngine engine;
    private LanguageEngine languageEngine;
    private SystemConfig systemConfig;
    private EternalNatureAPI api;
    private VersionChecker.VersionMeta versionMeta = null;
    private boolean started = false;

    public SystemConfig getSystemConfig() {
        return systemConfig;
    }

    public EternalEngine getEngine() {
        return engine;
    }

    public EternalNatureAPI getApi() {
        return api;
    }

    public VersionChecker.VersionMeta getVersionMeta() {
        return versionMeta;
    }

    /**
     * @return the language engine.
     */
    public LanguageEngine getLanguageEngine() {
        return languageEngine;
    }

    @Override
    public void onEnable() {
        MinecraftVersion serverVer = MinecraftVersion.getServerVersion();

        // Make sure the server is new enough to run the plugin
        if (serverVer.isBehindVersion(REQUIRED_VER)) {
            getLogger().warning("Unsupported version!"
                    + " This version requires the server "
                    + "to be running at least "
                    + REQUIRED_VER.getName());
            getPluginLoader().disablePlugin(this);
            return;
        }

        started = true;

        // Init language engine
        File langFolder = new File(getDataFolder(), "lang");
        languageEngine = new LanguageEngine(langFolder, "en");

        // Save internal resource.lang files externally
        String[] langFiles = new String[] {"en"};
        for (String s : langFiles) {
            File destination = new File(langFolder, s + ".lang");
            if (destination.exists()) continue;
            URL path = getClass().getResource("/lang/" + s + ".lang");
            try {
                FileUtils.copyURLToFile(path, destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load languages
        languageEngine.loadLanguages();

        systemConfig = new SystemConfig(this);
        engine = new EternalEngine(this);
        api = new EternalNatureAPI(this);

        engine.start();

        registerCommands(new HydrateCommand(this), new NatureCommand(this));
        registerListeners(new MoveListener(this), new ConnectionListener(this),
                new ConsumeListener(this), new ChunkListener(this), new BlockListener(this),
                new ItemListener(this), new DeathListener(this), new InteractListener(this));

        // Only check for updates if enabled.
        if (systemConfig.isEnabled(ConfigOption.UPDATES_CHECK)) {
            new VersionChecker(PluginData.RESOURCE_ID, getDescription().getVersion()).run(s -> {
                if (s.getState() == VersionChecker.VersionState.UNKNOWN) {
                    getLogger().log(Level.WARNING, "Failed to check plugin version. Are you running offline?");
                } else if (s.getState() == VersionChecker.VersionState.DEV_BUILD) {
                    ConsoleCommandSender sender = Bukkit.getConsoleSender();
                    sender.sendMessage(StringUtil.color("[EternalNature] \u00A7cYou are using a development build! " +
                            "Bug are to be expected, please report them."));
                } else if (s.getState() == VersionChecker.VersionState.BEHIND) {
                    ConsoleCommandSender sender = Bukkit.getConsoleSender();
                    sender.sendMessage(StringUtil.color(""));
                    sender.sendMessage(StringUtil.color("&e New update available for " + getDescription().getName()));
                    sender.sendMessage(StringUtil.color(" Current version: &e" + s.getCurrentVersion()));
                    sender.sendMessage(StringUtil.color(" Latest version: &e" + s.getLatestVersion()));
                    sender.sendMessage(StringUtil.color(""));
                }
                this.versionMeta = s;
            });
        }
        
        // Enable metrics
        new Metrics(this);
    }

    @Override
    public void onDisable() {
        if (started) engine.shutdown();
    }

    protected void registerCommands(EternalCommand... commands) {
        for (EternalCommand cmd : commands) {
            PluginCommand pc = Bukkit.getPluginCommand(cmd.getName());
            if (pc == null) continue;
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }
    }

    protected void registerListeners(Listener... listeners) {
        PluginManager manager = Bukkit.getPluginManager();
        for (Listener l : listeners) manager.registerEvents(l, this);
    }
}
