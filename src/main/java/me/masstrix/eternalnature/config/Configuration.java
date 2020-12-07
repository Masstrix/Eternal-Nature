package me.masstrix.eternalnature.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

/**
 * Handles the reloading of a config file. When ever the reload method is called
 * then it will update all subscribed {@link Configurable}.
 */
public class Configuration {

    private final Plugin plugin;
    private final Set<Configurable> configurables;
    private final File file;
    private YamlConfiguration config;

    /**
     * Creates a new configuration. This works from the plugins data folder.
     *
     * @param plugin plugin the configuration is being created from.
     * @param name   name of the config. This can include a path (eg. <i>data/config</i>)
     *               and the .yml extension at the end is optional as it will be added it
     *               it is not already. Both <i>config</i> and <i>config.yml</i> will read
     *               the same file.
     */
    public Configuration(Plugin plugin, String name) {
        this(plugin, new File(plugin.getDataFolder(), name.endsWith(".yml") ? name : name + ".yml"));
    }

    /**
     * Creates a new configuration.
     *
     * @param plugin plugin the configuration is being created from.
     * @param file   file where the config is stored.
     */
    public Configuration(Plugin plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        configurables = new TreeSet<>((incoming, checked) -> {
            if (incoming == checked) return 0;
            Configurable.Before before = incoming.getClass().getAnnotation(Configurable.Before.class);
            return before != null && before.value().equals(checked.getConfigPath()) ? -1 : 1;
        });
    }

    /**
     * Creates a new file if one does not already exist and loads it.
     *
     * @param resource if true then a resource file will be copied into the
     *                 directory if it does not exist.
     * @return an instance of this configuration.
     */
    public Configuration create(boolean resource) {
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            if (resource) {
                plugin.saveResource(file.getName(), false);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        if (resource) {
            config.options().copyDefaults(true).copyHeader(true);
            InputStream def = plugin.getResource(file.getName());
            if (def != null) {
                InputStreamReader reader = new InputStreamReader(def);
                config.setDefaults(YamlConfiguration.loadConfiguration(reader));
            }
            save();
        }
        return this;
    }

    /**
     * Subscribes a configurable to the handler. When subscribed the configurable
     * will have {@link Configurable#updateConfig(ConfigurationSection)} called when
     * ever the {@link #reload()} method is called for this handler.
     *
     * @param configurable configurable to subscribe to the handler.
     */
    public void subscribe(Configurable configurable) {
        configurables.add(configurable);
    }

    /**
     * Unsubscribes a configurable from the handler.
     *
     * @param configurable configurable to unsubscribe from the handler.
     */
    public void unsubscribe(Configurable configurable) {
        configurables.remove(configurable);
    }

    /**
     * @return the yml config.
     */
    public YamlConfiguration getYml() {
        return config;
    }

    /**
     * Sets a value in the configuration.
     *
     * @param path path to the value.
     * @param val  value to set.
     */
    public void set(String path, Object val) {
        config.set(path, val);
    }

    public Object get(String path) {
        return config.get(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    /**
     * Toggles a boolean value in the config.
     *
     * @param path path to boolean to toggle.
     * @return the new value of the boolean.
     */
    public boolean toggle(String path) {
        boolean invert = !config.getBoolean(path);
        config.set(path, invert);
        return invert;
    }

    /**
     * Reloads the config and updates all subscribed configurables.
     */
    public void reload() {
        create(false);
        if (config == null) return;
        configurables.forEach(this::reload);
    }

    /**
     * Reloads a single configuration. If the section the configurable is asking
     * for does not exist a new section will be created to accommodate it.
     *
     * @param configurable configurable to reload.
     */
    public void reload(Configurable configurable) {
        Configurable.Path pathVar = configurable.getClass().getAnnotation(Configurable.Path.class);
        String path = pathVar != null ? pathVar.value() : configurable.getConfigPath();
        ConfigurationSection sec = config;
        if (path != null && !path.isEmpty()) {
            if (!config.isConfigurationSection(path))
                sec = config.createSection(path);
            else
                sec = config.getConfigurationSection(path);
        }
        configurable.updateConfig(sec);
    }

    /**
     * Saves the configuration to file.
     */
    public Configuration save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
}
