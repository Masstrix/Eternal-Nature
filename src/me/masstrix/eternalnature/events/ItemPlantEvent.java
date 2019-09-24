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

package me.masstrix.eternalnature.events;

import me.masstrix.eternalnature.core.world.PlantType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;

public class ItemPlantEvent extends EternalEvent implements Cancellable {

    private boolean cancelled;
    private Location loc;
    private PlantType plantType;
    private Material material;

    public ItemPlantEvent(Location loc, PlantType type, Material material) {
        this.loc = loc;
        this.plantType = type;
        this.material = material;
    }

    public Location getLocation() {
        return loc;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public Material getItemType() {
        return material;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
