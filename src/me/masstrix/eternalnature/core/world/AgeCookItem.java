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
import me.masstrix.eternalnature.events.ItemBakeEvent;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class AgeCookItem implements AgeItem {

    private Item item;
    private double totalTemp;
    private int ticks = 0;
    private int ticksRandom;
    private boolean baked;
    private EternalNature plugin;

    public AgeCookItem(EternalNature plugin, Item item) {
        this.plugin = plugin;
        this.item = item;
        this.ticksRandom = MathUtil.randomInt(5, 10);
    }

    /**
     * @return if the item is still valid.
     */
    @Override
    public boolean isValid() {
        return item.isValid();
    }

    /**
     * @return if the item has been baked or not.
     */
    @Override
    public boolean isDone() {
        return baked;
    }

    /**
     * Returns if the item can be cooked. If the area is simply not hot enough then
     * it should not be used for the cooking process.
     *
     * @return if the area is hot enough to cook the item.
     */
    public boolean isHotEnough() {
        WorldData data = plugin.getEngine().getWorldProvider().getWorld(item.getWorld());
        Location loc = item.getLocation();
        double localTemp = data.getBiomeTemperature(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        return localTemp > 25;
    }

    /**
     * Tick the item to progress. If the items total temperature is above a certain threshold
     * and has been ticked enough times then it will be baked and return <code>BAKED</code> state.
     *
     * @return the state after the item has been ticked.
     */
    @Override
    public AgeProcessState tick() {
        if (!isValid()) return AgeProcessState.INVALID;
        if (++ticks < ticksRandom) return AgeProcessState.AGING;
        if (baked) return AgeProcessState.COMPLETE;

        // Get the local temperature for the item.
        WorldData data = plugin.getEngine().getWorldProvider().getWorld(item.getWorld());
        Location loc = item.getLocation();
        double localTemp = data.getBiomeTemperature(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        totalTemp += localTemp;

        // Bake the item if it has become hot enough.
        if (totalTemp >= 100) {
            ItemBakeEvent event = new ItemBakeEvent(item);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return AgeProcessState.COMPLETE;
            }
            baked = true;
            ItemStack stack = item.getItemStack();
            Material product = AgingItemWorker.getCookedProduct(stack.getType());
            if (stack.getType() != product) {
                stack.setType(product);
            }
            item.setItemStack(stack);
            return AgeProcessState.COMPLETE;
        }
        return AgeProcessState.AGING;
    }

    @Override
    public int hashCode() {
        return item.hashCode() + super.hashCode();
    }
}
