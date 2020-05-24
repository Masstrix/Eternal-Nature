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
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AutoPlanter implements EternalWorker {

    private EternalNature plugin;
    private SystemConfig config;
    private Set<Plant> plants = new HashSet<>();
    private BukkitTask task;

    public AutoPlanter(EternalNature plugin) {
        this.plugin = plugin;
        this.config = plugin.getSystemConfig();
    }

    /**
     * Loops over every item in the server and try's to load them as a plant. Only
     * items that are valid will be added to the auto planter.
     */
    private void findPlants() {
        if (!isEnabled()) return;
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof Item) {
                    if (attemptToAddItem((Item) e))
                        count++;
                }
            }
        }
        plugin.getLogger().info("Loaded " + count + " plants");
    }

    /**
     * Attempts to plant an item. If the item is not a valid plant then it will
     * be ignored otherwise it will have a chance as defined in the config to be
     * auto planted if auto planting is enabled.
     *
     * @param item item to attempt to plant.
     * @return if the item was added to the auto planter list to be planted.
     */
    public boolean attemptToAddItem(Item item) {
        if (!isEnabled()) return false;
        Material type = item.getItemStack().getType();
        PlantType plantType = PlantType.fromMaterial(type);
        if (plantType == null) return false;

        ConfigOption option = Plant.configOptionFromPlant(type);
        if (option == null) return false;
        double chance = config.getDouble(option);
        if (chance == 0) return false;

        // Rolls a random chance.
        if (MathUtil.chance(config.getDouble(option))) {
            plants.add(new Plant(item, plantType));
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        if (!isEnabled() || task != null) return;
        plugin.getLogger().info("Started auto planter");
        findPlants();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Plant p : new ArrayList<>(plants)) {
                    if (!p.isValid() || p.plant()) {
                        plants.remove(p);
                    }
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void end() {
        task.cancel();
        task = null;
    }

    /**
     * @return if auto planting is enabled.
     */
    public boolean isEnabled() {
        return config.isEnabled(ConfigOption.AUTO_PLANT);
    }
}
