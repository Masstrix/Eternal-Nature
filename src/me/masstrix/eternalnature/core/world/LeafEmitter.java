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
import me.masstrix.eternalnature.core.ConfigReloadUpdate;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.core.entity.EntityStorage;
import me.masstrix.eternalnature.core.render.LeafParticle;
import me.masstrix.eternalnature.data.UserData;
import me.masstrix.eternalnature.util.BlockScanner;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class LeafEmitter implements EternalWorker, ConfigReloadUpdate {

    private int spawnChance = 300;
    private int maxParticles = 200;
    private int scanDelay = 20 * 5;
    private EternalNature plugin;
    private Set<Location>  locations = new HashSet<>();
    private Set<LeafParticle> effects = new HashSet<>();
    private BukkitTask updater, spawner;
    private EntityStorage storage;
    private BlockScanner scanner;

    public LeafEmitter(EternalNature plugin, EntityStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.scanner = new BlockScanner(plugin);
        updateSettings();
    }

    @Override
    public void updateSettings() {
        SystemConfig config = plugin.getSystemConfig();
        scanDelay = config.getInt(ConfigOption.LEAF_EFFECT_SCAN_DELAY);
        maxParticles = config.getInt(ConfigOption.LEAF_EFFECT_MAX_PARTICLES);
        spawnChance = config.getInt(ConfigOption.LEAF_EFFECT_CHANCE);
        int range = config.getInt(ConfigOption.LEAF_EFFECT_RANGE);
        scanner.setScanScale(range, range);
        scanner.setFidelity(config.getInt(ConfigOption.LEAF_EFFECT_RANGE));
    }

    @Override
    public void start() {
        if (updater != null)
            updater.cancel();
        updater = new BukkitRunnable() { // Auto scan around players for new leaves.
            int ticks = scanDelay;
            int passed = 0;
            @Override
            public void run() {
                if (passed++ >= ticks) {
                    passed = 0;
                    if (!plugin.getSystemConfig().isEnabled(ConfigOption.LEAF_EFFECT)) return;
                    scan();
                    // Reduce the amount of scans being done as more players are online
                    int online = Bukkit.getOnlinePlayers().size();
                    if (online > 20) {
                        ticks = (online / 5) + scanDelay;
                    }
                }
            }
        }.runTaskTimer(plugin, 10, 1);

        // Stop a spawner if it has already been started.
        if (spawner != null)
            spawner.cancel();

        // Spawn leaf effects in valid locations found.
        spawner = new BukkitRunnable() {
            @Override
            public void run() {
                Set<LeafParticle> dead = new HashSet<>();
                for (LeafParticle effect : effects) {
                    effect.tick();
                    if (!effect.isAlive())
                        dead.add(effect);
                }
                effects.removeAll(dead);
                if (effects.size() >= maxParticles) return;
                if (!plugin.getSystemConfig().isEnabled(ConfigOption.LEAF_EFFECT)) return;
                for (Location loc : locations) {
                    if (MathUtil.chance(spawnChance)) {
                        effects.add(new LeafParticle(storage, plugin, loc));
                    }
                }
            }
        }.runTaskTimer(plugin, 30, 1);
    }

    /**
     * Scans an area around all players and checks to see if there are any valid leaves to
     * spawn leafs from.
     */
    public void scan() {
        locations.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Ignore afk players.
            UserData userData = plugin.getEngine().getUserData(player.getUniqueId());
            if (userData.getPlayerIdleInfo().isDeepIdle()) continue;

            // Set the location of the player and scan around them.
            scanner.setLocation(player.getLocation());
            scanner.scan(block -> {
                Block below = block.getRelative(BlockFace.DOWN);
                if (block.getBlockData() instanceof Leaves && below.isPassable())
                    locations.add(block.getLocation().add(0.5, 0, 0.5));
            });
        }
    }

    /**
     * Clears all effects and ends all running tasks.
     */
    @Override
    public void end() {
        updater.cancel();
        spawner.cancel();
        locations.clear();
        effects.forEach(LeafParticle::remove);
        effects.clear();
    }
}
