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
import me.masstrix.eternalnature.core.temperature.TemperatureIcon;
import me.masstrix.eternalnature.listeners.*;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import me.masstrix.version.MinecraftRelease;
import me.masstrix.version.MinecraftVersion;
import me.masstrix.version.checker.VersionCheckInfo;
import me.masstrix.version.checker.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class EternalNature extends JavaPlugin {

    private static final MinecraftVersion REQUIRED_VER = new MinecraftVersion("1.14");
    private EternalEngine engine;
    private LanguageEngine languageEngine;
    private SystemConfig systemConfig;
    private EternalNatureAPI api;
    private VersionCheckInfo versionCheckInfo = null;
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

    public VersionCheckInfo getVersionInfo() {
        return versionCheckInfo;
    }

    /**
     * @return the language engine.
     */
    public LanguageEngine getLanguageEngine() {
        return languageEngine;
    }

    @Override
    public void onEnable() {
        MinecraftVersion serverVer = MinecraftRelease.getServerVersion();

        // Make sure the server is new enough to run the plugin
        if (serverVer.isBehind(REQUIRED_VER)) {
            getLogger().warning("Unsupported version!"
                    + " This version requires the server "
                    + "to be running at least "
                    + REQUIRED_VER.getName());
            getPluginLoader().disablePlugin(this);
            return;
        }

        started = true;

        systemConfig = new SystemConfig(this);

        // Init language engine
        File langFolder = new File(getDataFolder(), "lang");
        languageEngine = new LanguageEngine(langFolder, "en");
        writeLangFiles(false);
        // Load languages
        languageEngine.loadLanguages();
        languageEngine.setLanguage(systemConfig.getString(ConfigOption.LANGUAGE));
        TemperatureIcon.reloadLang(languageEngine);
        engine = new EternalEngine(this);
        api = new EternalNatureAPI(this);

        engine.start();

        registerCommands(new HydrateCommand(this), new NatureCommand(this));
        registerListeners(new MoveListener(this), new ConnectionListener(this),
                new ConsumeListener(this), new ChunkListener(this), new BlockListener(this),
                new ItemListener(this), new DeathListener(this), new InteractListener(this));

        // Only check for updates if enabled.
        if (systemConfig.isEnabled(ConfigOption.UPDATES_CHECK)) {
            new VersionChecker(PluginData.RESOURCE_ID, getDescription().getVersion()).run(info -> {
                if (info.isUnknown()) {
                    getLogger().log(Level.WARNING, "Failed to check plugin version. Are you running offline?");
                }
                else if (info.isDev()) {
                    getLogger().log(Level.WARNING, "You are running a development build. Expect extra bugs.");
                }
                else if (info.isLatest()) {
                    getLogger().log(Level.INFO, "Plugin is up to date.");
                }
                else if (info.isBehind()) {
                    ConsoleCommandSender sender = Bukkit.getConsoleSender();
                    sender.sendMessage(StringUtil.color(""));
                    sender.sendMessage(StringUtil.color("&e New update available for " + getDescription().getName()));
                    sender.sendMessage(StringUtil.color(" Current version: &e" + info.getCurrent().getName()));
                    sender.sendMessage(StringUtil.color(" Latest version: &e" + info.getLatest().getName()));
                    sender.sendMessage(StringUtil.color(""));
                }

                this.versionCheckInfo = info;
            });
        }
        
        // Enable metrics
        new Metrics(this);
    }

    /**
     * Writes all internal .lang files to external lang folder to be edited.
     *
     * @param override should this override any .lang files that have
     *                 already been created.
     */
    public void writeLangFiles(boolean override) {
        File langFolder = new File(getDataFolder(), "lang");
        // Save internal resource.lang files externally
        String[] langFiles = new String[] {"en"};
        for (String s : langFiles) {
            File destination = new File(langFolder, s + ".lang");
            if (!override && destination.exists()) continue;
            try {
                URL url = getClass().getResource("/lang/" + s + ".lang");

                InputStreamReader streamReader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader);

                //List<String> lines = Files.readAllLines(path);
                BufferedWriter writer = new BufferedWriter(new FileWriter(destination));

                // Write file data
                for (String line; (line = reader.readLine()) != null;) {
                    writer.write(line);
                    writer.newLine();
                }

                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
