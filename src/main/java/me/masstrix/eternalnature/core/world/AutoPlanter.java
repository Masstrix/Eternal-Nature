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
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Configurable.Path("global.auto-plant")
public class AutoPlanter implements EternalWorker, Configurable {

    private final EternalNature plugin;
    private final Set<Plant> plants = new HashSet<>();
    private final Configuration CONFIG;
    private BukkitTask task;

    private boolean enabled;
    private boolean playSounds;
    private Map<String, Double> autoPlantChances = new HashMap<>();

    public AutoPlanter(EternalNature plugin) {
        this.plugin = plugin;
        CONFIG = plugin.getRootConfig();
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        playSounds = section.getBoolean("play-sound");
        List<String> exclude = Arrays.asList("enabled", "play-sound", "replant-crops");
        for (String key : section.getKeys(false)) {
            if (exclude.contains(key)) continue;
            autoPlantChances.put(key, section.getDouble(key));
        }
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

        String option = Plant.configPathFromPlant(type);
        if (option == null) return false;

        // Rolls a random chance.
        if (MathUtil.chance(autoPlantChances.getOrDefault(option, 0D))) {
            plants.add(new Plant(this, item, plantType));
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
                plants.removeIf(p -> !p.isValid() || p.plant());
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void end() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * @return if sounds should be played when a plant is planted.
     */
    public boolean getPlaySounds() {
        return playSounds;
    }

    /**
     * @return if auto planting is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
