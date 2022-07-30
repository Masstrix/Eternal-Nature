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

import me.masstrix.eternalnature.command.EternalCommand;
import me.masstrix.eternalnature.command.HydrateCommand;
import me.masstrix.eternalnature.command.NatureCommand;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.metric.Metrics;
import me.masstrix.eternalnature.core.temperature.TemperatureIcon;
import me.masstrix.eternalnature.external.PlaceholderSupport;
import me.masstrix.eternalnature.listeners.*;
import me.masstrix.eternalnature.log.DebugLogger;
import me.masstrix.eternalnature.trigger.TriggerManager;
import me.masstrix.eternalnature.util.BuildInfo;
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

    private static final MinecraftVersion REQUIRED_VER = new MinecraftVersion("1.19");
    private static final MinecraftVersion LATEST_BUILD = new MinecraftVersion("1.19.1");
    private static boolean serverVerUnTested;

    private EternalEngine engine;
    private LanguageEngine languageEngine;
    private VersionCheckInfo versionCheckInfo = null;
    private DebugLogger debugLogger;
    private TriggerManager triggerManager;

    private Configuration playerCfg;
    private Configuration config;

    public EternalEngine getEngine() {
        return engine;
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

    public Configuration getPlayerConfig() {
        return playerCfg;
    }

    public Configuration getRootConfig() {
        return config;
    }

    public DebugLogger getDebugLogger() {
        return debugLogger;
    }

    public TriggerManager getTriggerManager() {
        return triggerManager;
    }

    public static boolean isIsServerVerUnTested() {
        return serverVerUnTested;
    }

    @Override
    public void onLoad() {
        BuildInfo.load(this);
        debugLogger = new DebugLogger(this);
        debugLogger.info("----------------------------------------");
        debugLogger.info("Plugin Information");
        debugLogger.info("Name: " + getDescription().getName());
        debugLogger.info("Version: " + getDescription().getVersion());
        debugLogger.info("Build Kind: " + BuildInfo.getBuildKind());
        debugLogger.info("----------------------------------------");
        
        // This will make sure only 30 days worth of logs are kept. Any log files that are older than
        // 30 days will be deleted.
        debugLogger.cleanOldLogs(5);
    }

    @Override
    public void onEnable() {
        BuildInfo.load(this);
        debugLogger.info("Enabling Plugin");
        MinecraftVersion serverVer = MinecraftRelease.getServerVersion();

        // Make sure the server is new enough to run the plugin
        if (serverVer.isBehind(REQUIRED_VER)) {
            getLogger().warning("Unsupported version!"
                    + " This version requires the server "
                    + "to be running at least "
                    + REQUIRED_VER.getName());
            debugLogger.warning("Server is running a not supported version. Disabling plugin.");
            debugLogger.info("--------------------------------------");
            debugLogger.info("server   version: " + serverVer);
            debugLogger.info("required version: " + REQUIRED_VER);
            debugLogger.info("--------------------------------------");
            getPluginLoader().disablePlugin(this);
            return;
        }

        // If the server version is ahead of the latest build version send a message saying
        // that the plugin has not been tested on this version and it may not function as
        // expected.
        if (serverVer.isAhead(LATEST_BUILD)) {
            getLogger().warning(
                    "This version of minecraft has not been tested yet for this " +
                    "plugin and so there might be issues. " +
                    "If you have issues running the plugin on your server support will not be given.");
            serverVerUnTested = true;
        }

        config = new Configuration(this, "config").create(true);
        playerCfg = new Configuration(this, "players").create(false);

        // Init language engine
        File langFolder = new File(getDataFolder(), "lang");
        languageEngine = new LanguageEngine(langFolder, "en").setLogger(getLogger());
        writeLangFiles(false);
        // Load languages
        languageEngine.loadLanguages();
        languageEngine.setLanguage(getConfig().getString("general.language"));
        TemperatureIcon.reloadLang(languageEngine);

        engine = new EternalEngine(this);

        registerCommands(new HydrateCommand(this), new NatureCommand(this));
        registerListeners(new MoveListener(this), new ConnectionListener(this),
                new ConsumeListener(this), new BlockListener(this),
                new ItemListener(this), new DeathListener(this),
                new InteractListener(this));

        // Only check for updates if enabled.
        if (getConfig().getBoolean("general.check-for-updates")) {
            String ver = getDescription().getVersion().replace("-SNAPSHOT", "");
            new VersionChecker(PluginData.RESOURCE_ID, ver).run(info -> {
                if (info.isUnknown()) {
                    getLogger().log(Level.WARNING, "Failed to check plugin version. Are you running offline?");
                    debugLogger.warning("Failed to check version.");
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
                debugLogger.info("Finished checking plugin version.");
            });
        }
        
        // Enable metrics
        new Metrics(this);

        // Register placeholders if plugin is installed
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderSupport(this).register();
        }

        triggerManager = new TriggerManager(this);
        triggerManager.load();

        engine.start();
        config.subscribe(TemperatureIcon.BURNING);
        config.save();
        config.reload();
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
        String[] langFiles = new String[] {"en", "es", "zh_CN"};
        for (String s : langFiles) {
            File destination = new File(langFolder, s + ".lang");
            if (!override && destination.exists()) continue;
            try {
                URL url = getClass().getResource("/lang/" + s + ".lang");
                debugLogger.info("Writing language file " + s + ".lang");

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
                debugLogger.error("Failed to write file.", e);
            }
        }
    }

    @Override
    public void onDisable() {
        if (engine != null) engine.shutdown();
        triggerManager.clear();
        debugLogger.info("Shutdown Plugin");
        debugLogger.close();
    }

    private void registerCommands(EternalCommand... commands) {
        for (EternalCommand cmd : commands) {
            PluginCommand pc = Bukkit.getPluginCommand(cmd.getName());
            if (pc == null) continue;
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }
    }

    protected void registerListeners(Listener... listeners) {
        PluginManager manager = Bukkit.getPluginManager();
        for (Listener l : listeners) {
            manager.registerEvents(l, this);
            if (Configurable.class.isAssignableFrom(l.getClass())) {
                config.subscribe((Configurable) l);
            }
        }
    }
}
