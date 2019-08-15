package me.masstrix.eternalnature.config;

import me.masstrix.eternalnature.EternalNature;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SystemConfig {

    private static final int configVersion = 2;
    private EternalNature plugin;
    private FileConfiguration config;

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
    }

    public Object get(ConfigOption option) {
        return config.get(option.key, option.def);
    }

    public boolean equals(ConfigOption option, Object o) {
        return get(option).equals(o);
    }

    public boolean isEnabled(ConfigOption option) {
        return getBoolean(option);
    }

    public void set(ConfigOption option, Object o) {
        config.set(option.key, o);
    }

    public boolean toggle(ConfigOption option) {
        boolean b = !getBoolean(option);
        config.set(option.key, b);
        return b;
    }

    public boolean getBoolean(ConfigOption option) {
        return config.getBoolean(option.key, (Boolean) option.def);
    }

    public String getString(ConfigOption option) {
        return config.getString(option.key, (String) option.def);
    }

    public int getInt(ConfigOption option) {
        return config.getInt(option.key, (Integer) option.def);
    }

    public StatusRenderMethod getRenderMethod(ConfigOption option) {
        return StatusRenderMethod.getOr(get(option).toString(), StatusRenderMethod.BOSSBAR);
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
