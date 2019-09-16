package me.masstrix.eternalnature;

import me.masstrix.eternalnature.command.*;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.metric.Metrics;
import me.masstrix.eternalnature.listeners.*;
import me.masstrix.eternalnature.menus.SettingsMenu;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.eternalnature.util.VersionChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class EternalNature extends JavaPlugin {

    private EternalEngine engine;
    private SystemConfig systemConfig;
    private EternalNatureAPI api;
    private VersionChecker.VersionMeta versionMeta = null;
    private SettingsMenu settingsMenu;

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

    public SettingsMenu getSettingsMenu() {
        return settingsMenu;
    }

    @Override
    public void onEnable() {
        systemConfig = new SystemConfig(this);
        engine = new EternalEngine(this);
        api = new EternalNatureAPI(this);
        settingsMenu = new SettingsMenu(this);

        engine.start();

        registerCommands(new HydrateCommand(this), new NatureCommand(this), new TestCommand());

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new MoveListener(this), this);
        manager.registerEvents(new ConnectionListener(this), this);
        manager.registerEvents(new ConsumeListener(this), this);
        manager.registerEvents(new ChunkListener(this), this);
        manager.registerEvents(new BlockListener(this), this);
        manager.registerEvents(new ItemListener(this), this);
        manager.registerEvents(settingsMenu, this);

        // Only check for updates if enabled.
        if (systemConfig.isEnabled(ConfigOption.UPDATES_CHECK)) {
            new VersionChecker(PluginData.RESOURCE_ID, getDescription().getVersion()).run(s -> {
                if (s.getState() == VersionChecker.PluginVersionState.UNKNOWN) {
                    getLogger().log(Level.WARNING, "Failed to check plugin version. Are you running offline?");
                } else if (s.getState() == VersionChecker.PluginVersionState.DEV_BUILD) {
                    ConsoleCommandSender sender = Bukkit.getConsoleSender();
                    sender.sendMessage(StringUtil.color("[EternalNature] \u00A7cYou are using a development build! " +
                            "Bug are to be expected, please report them."));
                } else if (s.getState() == VersionChecker.PluginVersionState.BEHIND) {
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
        engine.shutdown();
    }

    private void registerCommands(EternalCommand... commands) {
        for (EternalCommand cmd : commands) {
            PluginCommand pc = Bukkit.getPluginCommand(cmd.getName());
            if (pc == null) continue;
            pc.setExecutor(cmd);
            pc.setTabCompleter(cmd);
        }
    }
}
