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
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.events.SaplingSpreadEvent;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Handles randomly spreading palings from trees.
 */
@Configurable.Path("global.randomly-spread-trees")
public class TreeSpreader implements EternalWorker, Configurable {

    private BukkitTask task;
    private EternalNature plugin;

    private boolean enabled;
    private int range;
    private int scans;

    public TreeSpreader(EternalNature plugin) {
        this.plugin = plugin;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        range = section.getInt("range");
        scans = section.getInt("scans");
    }

    @Override
    public void start() {
        if (!enabled || task != null) return;
        plugin.getLogger().info("Started Tree Spreader");
        final int delay = (20 * 30);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    doScan(player.getLocation().getBlock());
                }
            }
        }.runTaskTimer(plugin, delay, delay);
    }

    /**
     * Does a scan around a block. If any of them are leave blocks that have a passable
     * block below it then it will spawn a new sapling from the block. Any block that
     * is placed bu a player will be ignored in this scan.
     *
     * @param center center block to pick random locations from.
     */
    private void doScan(Block center) {
        for (int i = 0; i < scans; i++) {
            // Get a random vector in the scan range
            int randX = MathUtil.randomInt(range * 2) - range;
            int randY = MathUtil.randomInt(range * 2) - range;
            int randZ = MathUtil.randomInt(range * 3) - range;

            // Get the block and check if is of type leaves
            Block block = center.getWorld().getBlockAt(
                    randX + center.getX(),
                    randZ + center.getY(),
                    randY + center.getZ());
            BlockData data = block.getBlockData();
            if (!(data instanceof Leaves)) continue;

            Leaves leaves = (Leaves) data;
            if (leaves.isPersistent()) continue; // Ignore player placed leaves

            // Drop sapling if air is underneath leaf block
            if (block.getRelative(0, -1, 0).isPassable()) {
                Location loc = block.getLocation().add(0.5, -0.3, 0.5);
                SaplingSpreadEvent event = new SaplingSpreadEvent(loc);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) continue;
                loc = event.getLocation();
                Material product = TreeProduct.SAPLING.convert(block.getType());
                ItemStack drop = new ItemStack(product);
                block.getWorld().dropItem(loc, drop);
            }
        }
    }

    @Override
    public void end() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
