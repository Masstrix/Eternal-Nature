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

import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.core.Renderer;
import me.masstrix.eternalnature.core.UserWorker;
import me.masstrix.eternalnature.core.entity.shadow.ShadowEntityManager;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.world.*;
import me.masstrix.eternalnature.menus.MenuManager;
import me.masstrix.eternalnature.menus.settings.*;
import me.masstrix.eternalnature.player.UserData;
import me.masstrix.eternalnature.util.Stopwatch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class EternalEngine {

    private static boolean enabled = false;
    private EternalNature plugin;
    private WorldProvider worldProvider;
    private TemperatureProfile defaultTempProfile;
    private AutoPlanter autoPlanter;
    private MenuManager menuManager;
    private EternalHeartbeat heartbeat;

    private List<EternalWorker> workers = new ArrayList<>();
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
        this.defaultTempProfile = new TemperatureProfile(
                new Configuration(plugin, "temperature-config")
                        .create(true));
        this.menuManager = new MenuManager(plugin);
        heartbeat = new EternalHeartbeat(plugin, 10);

        AgingItemWorker agingItemWorker;
        LeafEmitter leafEmitter;
        TreeSpreader treeSpreader;
        registerWorkers(
                heartbeat,
                new UserWorker(plugin, this),
                new Renderer(plugin, this),
                new ShadowEntityManager(plugin),
                worldProvider = new WorldProvider(plugin),
                autoPlanter = new AutoPlanter(plugin),
                agingItemWorker = new AgingItemWorker(plugin),
                leafEmitter = new LeafEmitter(plugin),
                treeSpreader = new TreeSpreader(plugin));
        getWorker(TreeSpreader.class);

        Configuration config = plugin.getRootConfig();

        plugin.registerListeners(menuManager);
        menuManager.register(
                new SettingsMenu(plugin, menuManager),
                new HydrationSettingsMenu(plugin, menuManager),
                new TempSettingsMenu(plugin, menuManager),
                new LangSettingsMenu(plugin, menuManager),
                new LeafParticleMenu(plugin, menuManager),
                new OtherSettingsMenu(plugin, menuManager));
        menuManager.getMenus().forEach(config::subscribe);

        config.subscribe(autoPlanter);
        config.subscribe(worldProvider);
        config.subscribe(defaultTempProfile);
        config.subscribe(agingItemWorker);
        config.subscribe(leafEmitter);
        config.subscribe(treeSpreader);
    }

    public EternalNature getPlugin() {
        return plugin;
    }

    public EternalHeartbeat getHeartbeat() {
        return heartbeat;
    }

    public MenuManager getMenuManager() {
        return menuManager;
    }

    EternalEngine start() {
        loadPlayerData();
        workers.forEach(EternalWorker::start);
        return this;
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

        ConfigurationSection section = plugin.getPlayerConfig().getYml()
                .getConfigurationSection(uuid.toString());
        UserData user;
        if (section != null) {
            user = new UserData(plugin, uuid, section.getDouble("temp"), section.getDouble("hydration"));
            user.setThirstTimer(section.getInt("effects.thirst"));
        } else {
            user = new UserData(plugin, uuid);
        }

        users.put(uuid, user);
        plugin.getRootConfig().subscribe(user);
        return user;
    }

    /**
     * Gets a worker class.
     *
     * @param clazz class to get.
     * @return the loaded worker class or null if it's not a valid worker.
     */
    public EternalWorker getWorker(Class<? extends EternalWorker> clazz) {
        for (EternalWorker e : workers) {
            if (e.getClass().getCanonicalName().equals(clazz.getCanonicalName())) {
                return e;
            }
        }
        return null;
    }

    public TemperatureProfile getDefaultTempProfile() {
        return defaultTempProfile;
    }

    public WorldProvider getWorldProvider() {
        return worldProvider;
    }

    public AutoPlanter getAutoPlanter() {
        return autoPlanter;
    }

    /**
     * Shutdowns the engine and all threads or processes currently being
     * run by the plugin.
     */
    void shutdown() {
        workers.forEach(EternalWorker::end);
    }

    /**
     * Registers all the workers into a list to be started and shutdown.
     *
     * @param workers list of workers.
     */
    private void registerWorkers(EternalWorker... workers) {
        Collections.addAll(this.workers, workers);
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
        UserData user = users.get(uuid);
        plugin.getRootConfig().unsubscribe(user);
        user.endSession();
        users.remove(uuid);
    }

    public Collection<UserData> getCashedUsers() {
        return users.values();
    }
}
