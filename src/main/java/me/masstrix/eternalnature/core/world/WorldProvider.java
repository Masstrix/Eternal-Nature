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

package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalHeartbeat;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.Ticking;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.core.EternalWorker;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WorldProvider implements EternalWorker, Configurable {

    private final EternalNature plugin;
    private final EternalHeartbeat HEARTBEAT;
    private Map<UUID, WorldData> worldData = new HashMap<>();
    private int tick = 0;

    public WorldProvider(EternalNature plugin) {
        this.plugin = plugin;
        HEARTBEAT = new EternalHeartbeat(plugin, 1);
    }

    public EternalHeartbeat getHeartbeat() {
        return HEARTBEAT;
    }

    /**
     * @param world world to get data for.
     * @return the worlds data or null if the world does not exist.
     */
    public WorldData getWorld(World world) {
        if (world == null) return null;
        if (worldData.containsKey(world.getUID())) {
            return worldData.get(world.getUID());
        }

        WorldData data = new WorldData(plugin, world);
        worldData.put(world.getUID(), data);
        return data;
    }

    /**
     * Returns if a worlds data was loaded.
     *
     * @param world world to check if it is loaded.
     * @return if the worlds data is loaded.
     */
    public boolean isLoaded(World world) {
        return worldData.containsKey(world.getUID());
    }

    @Override
    public void start() {
        HEARTBEAT.start();
        for (World world : Bukkit.getWorlds()) {
            getWorld(world);
        }
    }

    @Override
    public void end() {
        worldData.forEach((name, world) -> world.unload());
        HEARTBEAT.end();
    }

    /**
     * @return how many worlds are loaded.
     */
    public int getLoaded() {
        return worldData.size();
    }

    public WorldData getFirstWorld() {
        if (worldData.size() == 0) return null;
        return getWorlds().iterator().next();
    }

    public Collection<WorldData> getWorlds() {
        return worldData.values();
    }

    public List<String> getWorldNames() {
        List<String> names = new ArrayList<>();
        worldData.values().forEach(w -> names.add(w.getWorldName()));
        return names;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        worldData.forEach((id, w) -> plugin.getRootConfig().reload(w));
    }
}
