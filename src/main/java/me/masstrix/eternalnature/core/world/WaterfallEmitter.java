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

import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.Position;
import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.io.*;
import java.util.List;
import java.util.UUID;

@Deprecated
public class WaterfallEmitter implements Serializable {

    Position pos;
    private Location location;
    private int height = -1;
    private int lastCheck;
    private boolean valid, hitsGround;

    public WaterfallEmitter(Location loc) {
        Block block = loc.getBlock();
        pos = new Position(block.getX(), block.getY(), block.getZ());
        this.location = block.getLocation().clone().add(0.5, 0, 0.5);
        if (block.getType() != Material.WATER) return;

        int height = 0;
        for(int i = 1; i <= 5; i++) {
            if (block.getRelative(0, i, 0).getType() != Material.WATER) {
                break;
            }
            height++;
        }

        if (height >= 3) {
            this.height = height;
            valid = true;
        }
    }

    public WaterfallEmitter(Location loc, int height) {
        this.location = loc;
        this.height = height;
        valid = loc != null && height > 0;
    }

    public Location getLocation() {
        return location;
    }

    public int getHeight() {
        return height;
    }

    public void tick() {
        World world = location.getWorld();
        if (!valid) return;
        if (world != null) {
            Location location = this.location.clone().add(0, 0.5, 0);

            int size = MathUtil.randomInt(3, 5);
            world.spawnParticle(Particle.REDSTONE, location, 2, 0.3, 0.3, 0.3,
                    new Particle.DustOptions(Color.fromRGB(255, 255, 255), size));
            world.spawnParticle(Particle.SPIT, location, 2, 0.2, 0.1, 0.2, 0.05);
            world.spawnParticle(Particle.WATER_SPLASH, location, 5, 0.3, 0, 0.3, 0.05);
        }
    }

    public boolean isValid() {
        if (!valid) return false;
        if (++lastCheck >= 3) {
            if (location == null || height == -1) {
                valid = false;
                return true;
            }
            valid = isRelativeValid(location.getBlock());
            lastCheck = 0;
        }

        return valid;
    }

    /**
     * Returns if the surrounding area is valid for this waterfall to still be alive.
     *
     * @param center center block to check against.
     * @return if the surrounding area is valid.
     */
    private boolean isRelativeValid(Block center) {
        if (center.getType() != Material.WATER) return false;
        Block under = center.getRelative(0, -1, 0);
        if (under.getBlockData() instanceof Levelled) {
            Levelled levelled = (Levelled) under.getBlockData();
            if (levelled.getLevel() == 0) return true;
            if (levelled.getLevel() > 7) return false;
            hitsGround = false;
        } else {
            hitsGround = true;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return pos == null ? 0 : pos.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WaterfallEmitter && o.hashCode() == this.hashCode();
    }

    public String serialize() {
        return String.format("[%s,%s,%s]", pos.getX(), pos.getY(), pos.getZ());
    }

    public static WaterfallEmitter deserialize(WorldData world, String string) {
        String[] data = string.substring(1, string.length() - 1).split(",");
        double x, y, z;
        if (!StringUtil.isInteger(data[0])) return null;
        x = Double.parseDouble(data[0]);
        y = Double.parseDouble(data[1]);
        z = Double.parseDouble(data[2]);
        return new WaterfallEmitter(new Location(world.asBukkit(), x, y, z));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(height);
        out.writeUTF(location.getWorld().getUID().toString());
        out.writeDouble(location.getX());
        out.writeDouble(location.getY());
        out.writeDouble(location.getZ());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.height = in.readInt();
        World world = Bukkit.getWorld(UUID.fromString(in.readUTF()));
        if (world == null)
            return;
        this.location = new Location(world, in.readDouble(), in.readDouble(), in.readDouble());
    }
}
