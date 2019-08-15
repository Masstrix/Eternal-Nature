package me.masstrix.eternalnature;

import me.masstrix.eternalnature.core.Renderer;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.core.UserWorker;
import me.masstrix.eternalnature.core.world.LeafEmitter;
import me.masstrix.eternalnature.core.world.SaplingPlanter;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.data.UserData;
import me.masstrix.eternalnature.util.Stopwatch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EternalEngine {

    private static boolean enabled = false;
    private EternalNature plugin;
    private Renderer renderer;
    private UserWorker userWorker;
    private WorldProvider worldProvider;
    private TemperatureData temperatureData;
    private SaplingPlanter saplingPlanter;
    private LeafEmitter leafEmitter;

    private Map<UUID, UserData> users = new HashMap<>();

    private EternalEngine() {}

    /**
     * Creates an instance of the engine and starts it.
     *
     * @param plugin instance of main class.
     */
    EternalEngine(EternalNature plugin) {
        if (enabled) return;
        enabled  = true;
        this.plugin = plugin;
        temperatureData = new TemperatureData(plugin);
        userWorker = new UserWorker(plugin, this);
        renderer = new Renderer(plugin, this);
        worldProvider = new WorldProvider(plugin);
        saplingPlanter = new SaplingPlanter(plugin);
        leafEmitter = new LeafEmitter(plugin);
    }

    void start() {
        loadPlayerData();
        userWorker.start();
        renderer.start();
        worldProvider.start();
        saplingPlanter.start();
        leafEmitter.start();
    }

    private void loadPlayerData() {
        plugin.getLogger().info("loading player data...");
        Stopwatch runtime = new Stopwatch().start();
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayerData(player.getUniqueId());
        }
        plugin.getLogger().info("loaded player data in " + runtime.stop() + "ms");
    }

    /**
     * Load a players data into cache.
     *
     * @param uuid uuid of player.
     * @return the players data.
     */
    public UserData loadPlayerData(UUID uuid) {
        if (users.containsKey(uuid)) {
            return users.get(uuid);
        }
        File file = new File(plugin.getDataFolder(), "players.yml");
        UserData user = null;
        if (file.exists()) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
                if (config.contains(uuid.toString())) {
                    user = new UserData(plugin, uuid,
                            (float) config.getDouble(uuid + ".temperature", 0),
                            (float) config.getDouble(uuid + ".temperature-to", 0),
                            (float) config.getDouble(uuid+ ".hydration", 0));
                }
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        if (user == null) user = new UserData(plugin, uuid);
        users.put(uuid, user);
        return user;
    }

    public TemperatureData getTemperatureData() {
        return temperatureData;
    }

    public WorldProvider getWorldProvider() {
        return worldProvider;
    }

    public SaplingPlanter getSaplingPlanter() {
        return saplingPlanter;
    }

    /**
     * Shutdowns the engine and all threads or processes currently being
     * run by the plugin.
     */
    void shutdown() {
        renderer.end();
        userWorker.end();
        worldProvider.end();
        saplingPlanter.end();
        leafEmitter.end();
    }

    /**
     * @param uuid uuid of the player.
     * @return the players data. Returns null if no data is loaded.
     */
    public UserData getUserData(UUID uuid) {
        return users.get(uuid);
    }

    /**
     * Unloads a users data from cache.
     *
     * @param uuid uuid of player being unloaded.
     */
    public void unloadUserData(UUID uuid) {
        users.get(uuid).endSession();
        users.remove(uuid);
    }

    public Collection<UserData> getCashedUsers() {
        return users.values();
    }
}
