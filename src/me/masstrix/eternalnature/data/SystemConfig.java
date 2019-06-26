package me.masstrix.eternalnature.data;

import me.masstrix.eternalnature.EternalNature;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SystemConfig {

    private static final int configVersion = 2;
    private EternalNature plugin;
    private FileConfiguration config;

    // General
    private boolean checkForUpdates = true;

    // Global
    private boolean thirstFlash = true;
    private boolean thirstDamage = true;

    // Render
    private boolean tempFlash = true;
    private boolean hydrationFlash = true;
    private StatusRenderMethod thirstRenderMethod = StatusRenderMethod.ACTIONBAR;
    private StatusRenderMethod tempRenderMethod = StatusRenderMethod.ACTIONBAR;

    // Environment
    private boolean waterfallsEnabled = true;

    // Advanced
    private int updateCalls = 10; // calls per second for updates
    private int renderCalls = 20; // calls per second for renders
    private int scanRadius = 10;

    public enum StatusRenderMethod {
        ACTIONBAR, BOSSBAR;

        static StatusRenderMethod getOr(String val, StatusRenderMethod def) {
            for (StatusRenderMethod m : StatusRenderMethod.values()) {
                if (m.name().equalsIgnoreCase(val)) {
                    return m;
                }
            }
            return def;
        }

        public StatusRenderMethod opisite() {
            return this == ACTIONBAR ? BOSSBAR : ACTIONBAR;
        }
    }

    public SystemConfig(EternalNature plugin) {
        this.plugin = plugin;
        loadConfig(false);
    }

    public void reload() {
        loadConfig(true);
    }

    private void loadConfig(boolean reload) {
        if (reload) plugin.reloadConfig();

        // Save config if none is present.
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists())
            plugin.saveResource("config.yml", false);

        // Validate config file.
        this.config = plugin.getConfig();
        if (!config.contains("version") || config.getInt("version") != configVersion) {
            rebuildConfig();
        }

        checkForUpdates = config.getBoolean("general.check-for-updates", true);

        if (config.contains("render.hydration.style"))
            thirstRenderMethod = StatusRenderMethod.getOr(
                    config.getString("render.hydration.style"), StatusRenderMethod.ACTIONBAR);
        if (config.contains("render.hydration.flash"))
            thirstFlash = config.getBoolean("render.hydration.flash");
        if (config.contains("global-settings.hydration.cause-damage"))
            thirstDamage = config.getBoolean("global-settings.hydration.cause-damage");

        if (config.contains("render.temperature.style"))
            tempRenderMethod = StatusRenderMethod.getOr(
                    config.getString("render.temperature.style"), StatusRenderMethod.ACTIONBAR);

        updateCalls = config.getInt("advanced.update-calls", 10);
        renderCalls = config.getInt("advanced.render-calls", 10);
        renderCalls = config.getInt("advanced.temperature.scan-radius", 10);

        waterfallsEnabled = config.getBoolean("environment.waterfalls", true);

        if (updateCalls > 20 || updateCalls < 0) updateCalls = 10;
        if (renderCalls > 20 || renderCalls < 0) renderCalls = 20;
    }

    public boolean checkForUpdates() {
        return checkForUpdates;
    }

    public int getUpdateCalls() {
        return 20 - updateCalls;
    }

    public int getRenderCalls() {
        return 20 - renderCalls;
    }

    public int getScanRadius() {
        return scanRadius;
    }

    public StatusRenderMethod getThirstRenderMethod() {
        return thirstRenderMethod;
    }

    public void setThirstRenderMethod(StatusRenderMethod method) {
        this.thirstRenderMethod = method;
        config.set("render.hydration.style", method.name());
    }

    public StatusRenderMethod getTempRenderMethod() {
        return tempRenderMethod;
    }

    public void setTempRenderMethod(StatusRenderMethod method) {
        this.tempRenderMethod = method;
        config.set("render.temperature.style", method.name());
    }

    public boolean isTempFlash() {
        return tempFlash;
    }

    public boolean isThirstFlash() {
        return thirstFlash;
    }

    public boolean isTempCatchFire() {
        return (boolean) config.get("global-settings.temperature.catch-fire", true);
    }

    public boolean isSweatEnabled() {
        return (boolean) config.get("global-settings.temperature.sweat", true);
    }

    public boolean tempCuaseDamage() {
        return (boolean) config.get("global-settings.temperature.cause-damage", true);
    }

    public boolean isHydrationEnabled() {
        return (boolean) config.get("global-settings.hydration.enabled", true);
    }

    public boolean isTempreatureEnabled() {
        return (boolean) config.get("global-settings.temperature.enabled", true);
    }

    public boolean isSprintingGains() {
        return (boolean) config.get("global-settings.temperature.sprinting", true);
    }

    public boolean isHydrationCauseDamage() {
        return thirstDamage;
    }

    public void setHydrationCauseDamage(boolean b) {
        this.thirstDamage = b;
        config.set("global-settings.hydration.cause-damage", b);
    }

    public boolean areWaterfallsEnabled() {
        return waterfallsEnabled;
    }

    public void setWaterfallsEnabled(boolean b) {
        waterfallsEnabled = b;
        config.set("environment.waterfalls", waterfallsEnabled);
    }

    public void save() {
        plugin.saveConfig();
    }

    public void rebuildConfig() {
        plugin.getLogger().info("Invalid config! Rebuilding config and copying data...");
        // Cache and reset old config
        Map<String, Object> data = new HashMap<>();
        for (String s : config.getKeys(true)) {
            if (s.equalsIgnoreCase("version")) continue;
            data.put(s, config.get(s));
        }
        plugin.saveResource("config.yml", true);
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Copy old values to new config file.
        for (Map.Entry<String, Object> value : data.entrySet()) {
            if (config.contains(value.getKey()))
                config.set(value.getKey(), value.getValue());
        }
        plugin.saveConfig();
        plugin.getLogger().info("Config has been fixed.");
    }
}
