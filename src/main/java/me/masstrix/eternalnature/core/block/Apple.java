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

package me.masstrix.eternalnature.core.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.util.Vector;

public class Apple extends GravityBlock {

    private ArmorStand stand;
    private boolean attached;
    private boolean onGround;
    private int ticksLived;
    private static final BlockData LEAVES_DATA = Material.OAK_LEAVES.createBlockData();

    public Apple(Location loc) {
        this(loc, new Vector());
    }

    public Apple(String data) {
        super(data);
    }

    public Apple(Location loc, Vector velocity) {
        super(loc, velocity);
        attached = true;
        stand = loc.getWorld().spawn(loc, ArmorStand.class, a -> {
            a.setSilent(true);
            a.setGravity(false);
            a.setVisible(false);
            a.setCollidable(false);
            a.setMarker(true);
        });
    }

    public void tick() {
        ticksLived++;
    }

    public void update() {
        World world = loc.getWorld();
        Block block = world.getBlockAt(loc.getBlockX(), loc.getBlockY() + 1, loc.getBlockZ());
        if (!block.getBlockData().matches(LEAVES_DATA)) {
            attached = false;
            gravity = true;
        }
    }
}
