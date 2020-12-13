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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configurable.Path("global")
public class AgingItemWorker implements EternalWorker, Configurable {

    private EternalNature plugin;
    private Set<AgeItem> items = new HashSet<>();
    private BukkitTask task;
    private boolean enabled;

    public AgingItemWorker(EternalNature plugin) {
        this.plugin = plugin;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("age-items");
    }

    /**
     * Finds ant items that can be aged in the server and adds them into a
     * list to slowly age.
     */
    private void findItems() {
        if (!enabled) return;
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof Item) {
                    if (attemptToAddItem((Item) e))
                        count++;
                }
            }
        }
        plugin.getLogger().info("Loaded " + count + " age-able items");
    }

    /**
     * Attempts to add an item into the age worker. If the item does not have a cook or
     * waste product then it will be ignored otherwise will begin aging.
     *
     * @param item
     * @return
     */
    public boolean attemptToAddItem(Item item) {
        if (item == null) return false;
        Material type = item.getItemStack().getType();
        Material product = getCookedProduct(type);
        if (product == type) return false;
        AgeCookItem cook = new AgeCookItem(plugin, item);
        if (cook.isHotEnough()) {
            items.add(cook);
        } else {
            product = getWasteProduct(type);
            if (product != type)
                items.add(new AgeRotItem(plugin, item));
        }
        return true;
    }

    @Override
    public void start() {
        if (!enabled || task != null) return;
        plugin.getLogger().info("Started food aging worker");
        findItems();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled) return;
                List<AgeItem> bin = new ArrayList<>();
                for (AgeItem i : items) {
                    if (!i.isValid() || i.isDone()) {
                        bin.add(i);
                        continue;
                    }
                    AgeItem.AgeProcessState state = i.tick();
                    if (state == AgeCookItem.AgeProcessState.COMPLETE || state == AgeCookItem.AgeProcessState.INVALID)
                        bin.add(i);
                }
                if (bin.size() > 0)
                    items.removeAll(bin);
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
     * Returns the waste product of an item. If the item has no waste product then
     * it's self will be returned. Wastes items are generally organic items that can
     * break down over time and rot such as meats and food.
     *
     * @param type type to get the waste product for,
     * @return material of product or <i>type</i> if it has none.
     */
    static Material getWasteProduct(Material type) {
        switch (type) {
            case CHICKEN:
            case BEEF:
            case RABBIT:
            case MUTTON:
            case PORKCHOP:
            case COD:
            case SALMON:
                return Material.ROTTEN_FLESH;
            case BREAD:
                return Material.WHEAT;
            default: return type;
        }
    }

    /**
     * Returns the items cooked product. If the item has no cooked product then
     * it's self will be returned.
     *
     * @param type type to get the product of.
     * @return material of product or <i>type</i> if it has none.
     */
    static Material getCookedProduct(Material type) {
        switch (type) {
            case POTATO: return Material.BAKED_POTATO;
            case CHICKEN: return Material.COOKED_CHICKEN;
            case BEEF: return Material.COOKED_BEEF;
            case RABBIT: return Material.COOKED_RABBIT;
            case PORKCHOP: return Material.COOKED_PORKCHOP;
            case SALMON: return Material.COOKED_SALMON;
            case COD: return Material.COOKED_COD;
            case MUTTON: return Material.COOKED_MUTTON;
            case KELP: return Material.DRIED_KELP;
            default:
                return type;
        }
    }
}
