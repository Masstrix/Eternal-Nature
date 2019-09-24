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

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.EternalWorker;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldProvider implements EternalWorker {

    private EternalNature plugin;
    private Map<UUID, WorldData> worldData = new HashMap<>();
    private BukkitTask ticker;
    private int tick = 0;

    public WorldProvider(EternalNature plugin) {
        this.plugin = plugin;
    }

    public WorldData getWorld(World world) {
        return getWorld(world.getUID());
    }

    /**
     * @param uuid name of the world. Case sensitive.
     * @return the worlds data.
     */
    public WorldData getWorld(UUID uuid) {
        if (worldData.containsKey(uuid)) {
            return worldData.get(uuid);
        }

        WorldData data = new WorldData(plugin, uuid);
        worldData.put(uuid, data);
        return data;
    }

    @Override
    public void start() {
        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                if (tick++ == 10) {
                    tick = 0;
                    worldData.forEach((n, w) -> w.tick());
                }
                worldData.forEach((n, w) -> w.render());
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public void end() {
        ChunkData.killProcesses();
        ticker.cancel();
        worldData.forEach((n, w) -> w.unload());
    }

    public int getLoaded() {
        return worldData.size();
    }
}
