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

package me.masstrix.eternalnature.config;

import me.masstrix.eternalnature.EternalNature;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a reference of the config.yml and implements methods to more safely access
 * those values.
 */
public class SystemConfig {

    /**
     * Version of the config. Maybe invalid?
     */
    private static final int configVersion = 2;
    private EternalNature plugin;
    private FileConfiguration config;

    public SystemConfig(EternalNature plugin) {
        this.plugin = plugin;
        loadConfig(false);
    }

    /**
     * Reloads the config.
     */
    public void reload() {
        loadConfig(true);
    }

    /**
     * Loads the config. Also sets it to copy default values across making
     * sure no options are missing from the config.
     *
     * @param reload is this a reload of the config.
     */
    private void loadConfig(boolean reload) {
        if (reload) plugin.reloadConfig();

        // Save config if none is present.
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists())
            plugin.saveDefaultConfig();

        // Validate config file.
        this.config = plugin.getConfig();
        this.config.options().copyDefaults(true);
        this.config.options().copyHeader(true);
        this.plugin.saveConfig();
        if (!config.contains("version") || config.getInt("version") != configVersion) {
            rebuildConfig();
        }
    }

    /**
     * Returns a value from the config as an object.
     *
     * @param option option to get the value of.
     * @return the config value as an object.
     */
    public Object get(ConfigOption option) {
        return config.get(option.key, option.def);
    }

    public boolean equals(ConfigOption option, Object o) {
        return get(option).equals(o);
    }

    /**
     * Returns if a boolean option is true.
     *
     * @param option option to check.
     * @return is option enabled.
     */
    public boolean isEnabled(ConfigOption option) {
        return getBoolean(option);
    }

    /**
     * Sets a value in the config.
     *
     * @param option option to set.
     * @param o object to set as the value.
     */
    public void set(ConfigOption option, Object o) {
        config.set(option.key, o);
    }

    /**
     * Toggles a boolean in the config.
     *
     * @param option option to toggle.
     * @return the inverted value for the option.
     */
    public boolean toggle(ConfigOption option) {
        boolean b = !getBoolean(option);
        config.set(option.key, b);
        return b;
    }

    /**
     * Return a boolean value from the config.
     *
     * @param option option to get.
     * @return true or false.
     */
    public boolean getBoolean(ConfigOption option) {
        return config.getBoolean(option.key, (Boolean) option.def);
    }

    /**
     * Return a string value from the config.
     *
     * @param option option to get.
     * @return a string.
     */
    public String getString(ConfigOption option) {
        return config.getString(option.key, (String) option.def);
    }

    /**
     * Return a integer from the config.
     *
     * @param option option to get.
     * @return a integer.
     */
    public int getInt(ConfigOption option) {
        return config.getInt(option.key, (Integer) option.def);
    }

    /**
     * Returns a double value from the config.
     *
     * @param option option to get.
     * @return a double.
     */
    public double getDouble(ConfigOption option) {
        return config.getDouble(option.key, (Double) option.def);
    }

    /**
     * Return a render method from a config value.
     *
     * @param option option to get.
     * @return a render method. defaults to {@link StatusRenderMethod#BOSSBAR}.
     */
    public StatusRenderMethod getRenderMethod(ConfigOption option) {
        return StatusRenderMethod.getOr(get(option).toString(), StatusRenderMethod.BOSSBAR);
    }

    /**
     * Saves the config.
     */
    public void save() {
        plugin.saveConfig();
    }

    @Deprecated
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
