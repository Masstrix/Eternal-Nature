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

import me.masstrix.eternalnature.api.EternalChunk;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.io.*;
import java.util.*;

public class ChunkData implements EternalChunk {

    private static final int VERSION = 2;
    private static final int sections = 16;

    private Set<WaterfallEmitter> waterfallEmitters = new HashSet<>();
    private WorldData worldData;
    private SystemConfig config;
    private final int x;
    private final int z;
    private long key;

    public ChunkData(WorldData worldData, int x, int z) {
        this.worldData = worldData;
        this.config = worldData.plugin.getSystemConfig();
        this.x = x;
        this.z = z;
        this.key = WorldData.pair(x, z);
    }

    /**
     * Attempts to read in a chunks data. If the data is invalid or the version of it is
     * not the latest version it will ignore the data and generate everything fresh.
     *
     * @param worldData world this chunk is for.
     * @param x x cords of the chunk.
     * @param z z cords of the chunk.
     * @param data data of the chunk.
     */
    public ChunkData(WorldData worldData, int x, int z, byte[] data) {
        this.worldData = worldData;
        this.x = x;
        this.z = z;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream in = new ObjectInputStream(inputStream);
            int version = in.readInt();
            if (version != VERSION) return; // Force regenerate chunk data
            Object waterfalls = in.readObject();
            if (waterfalls instanceof Set) {
                //noinspection unchecked
                waterfallEmitters = (Set<WaterfallEmitter>) waterfalls;
            }
        }
        catch (EOFException ignored) {
            worldData.plugin.getLogger().warning("Failed to load chunk " + x + ", " + z + " data");
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Renders all particles and effects within this chunk.
     */
    void render() {
        if (config.isEnabled(ConfigOption.WATERFALLS) && waterfallEmitters.size() > 0) {
            Set<WaterfallEmitter> clean = null;
            for (WaterfallEmitter waterfall : waterfallEmitters) {
                if (!waterfall.isValid()) {
                    if (clean == null) clean = new HashSet<>();
                    clean.add(waterfall);
                } else {
                    waterfall.tick();
                }
            }
            if (clean != null)
                waterfallEmitters.removeAll(clean);
        }
    }

    @Override
    public Set<WaterfallEmitter> getWaterfalls() {
        return waterfallEmitters;
    }

    void tick() {

    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public long getKey() {
        return key;
    }

    /**
     * Creates a new waterfall
     *
     * @param block flowing water block.
     */
    void createWaterfallEmitter(Block block) {
        if (block.getType() != Material.WATER) return;
        Levelled data = (Levelled) block.getBlockData();
        if (data.getLevel() < 8) return; // is the water not falling

        Block below = block.getRelative(0, -1, 0);
        if (below.getType() == Material.AIR) return; // Still more falling
        if (below.getType() == Material.WATER && ((Levelled) below.getBlockData()).getLevel() == 8) return;
        int height = 0;
        for (int i = 1; i < 4; i++) {
            Block r = block.getRelative(0, i, 0);
            if (r.getType() != Material.WATER) break;
            if (((Levelled) r.getBlockData()).getLevel() != 8) break;
            height++;
        }
        if (height < 3) return;
        waterfallEmitters.add(new WaterfallEmitter(block.getLocation(), height));
    }

    public static int getSection(double y) {
        int section = ((int) Math.floor(y)) / 16;
        return section > sections ? sections : section < 0 ? 0 : section;
    }
}
