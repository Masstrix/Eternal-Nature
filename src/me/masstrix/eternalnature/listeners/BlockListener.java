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
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.world.PlantType;
import me.masstrix.eternalnature.core.world.WaterfallEmitter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BlockListener implements Listener {

    private SystemConfig config;
    private EternalNature plugin;
    private List<WaterfallEmitter> locs = new ArrayList<>();

    public BlockListener(EternalNature plugin) {
        this.plugin = plugin;
        config = plugin.getSystemConfig();

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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        //calculateArea(event.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        //calculateArea(event.getBlock());

        Block block = event.getBlock();
        Location loc = block.getLocation();
        Material type = event.getBlock().getType();
        BlockData data = event.getBlock().getBlockData();

        // Replant crop if it's fully grown and auto replanting is enabled
        if (PlantType.isReplantableCrop(type) && data instanceof Ageable
                && config.isEnabled(ConfigOption.AUTO_REPLANT)) {
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
                }.runTaskLater(plugin, 2);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLiquid(PlayerBucketEmptyEvent event) {
        if (event.isCancelled()) return;
        //calculateArea(event.getBlockClicked().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(PlayerBucketFillEvent event) {
        if (event.isCancelled()) return;
        //calculateArea(event.getBlockClicked().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(BlockFromToEvent event) {
        if (event.isCancelled()) return;
        World world = event.getToBlock().getWorld();

        if (event.getBlock().getType() == Material.WATER) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getEngine().getWorldProvider().getWorld(world).createWaterfall(event.getBlock().getLocation().add(0, -1, 0));
                }
            }.runTaskLater(plugin, 5);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(BlockFadeEvent event) {
        if (event.isCancelled()) return;
        //calculateArea(event.getBlock());
    }

    private void calculateArea(Block block) {

    }
}
