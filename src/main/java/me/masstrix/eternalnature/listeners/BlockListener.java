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

package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigPath;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.core.world.PlantType;
import me.masstrix.eternalnature.core.world.WaterfallEmitter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BlockListener implements Listener, Configurable {

    private boolean autoRePlantCropEnabled;
    private final EternalNature PLUGIN;
    private List<WaterfallEmitter> locs = new ArrayList<>();

    public BlockListener(EternalNature plugin) {
        this.PLUGIN = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                List<Location> r = new ArrayList<>();
                for (WaterfallEmitter l : locs) {
                    l.tick();
                }
            }
        }.runTaskTimer(plugin, 1, 1);
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        autoRePlantCropEnabled = section.getBoolean(ConfigPath.AUTO_PLANT_CROPS);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !autoRePlantCropEnabled) return;

        Block block = event.getBlock();
        Location loc = block.getLocation();
        Material type = event.getBlock().getType();
        BlockData data = event.getBlock().getBlockData();

        // Replant crop if it's fully grown and auto replanting is enabled
        if (PlantType.isReplantableCrop(type) && data instanceof Ageable) {
            Ageable age = (Ageable) event.getBlock().getBlockData();
            if (age.getAge() != age.getMaximumAge()) return;
            boolean droppedSeed = false;

            List<ItemStack> drops = new ArrayList<>();

            // Remove 1 seed from the drops
            for (ItemStack drop : event.getBlock().getDrops()) {
                if (PlantType.isCropSeed(drop.getType())) {
                    droppedSeed = true;
                    if (drop.getAmount() > 1) {
                        drop.setAmount(drop.getAmount() - 1);
                        drops.add(drop);
                    }
                } else {
                    drops.add(drop);
                }
            }

            // Replant the drops
            if (droppedSeed) {
                event.setDropItems(false);
                drops.forEach(item -> block.getWorld().dropItemNaturally(loc, item));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        event.getBlock().setType(type);
                    }
                }.runTaskLater(PLUGIN, 2);
            }
        }
    }
}
