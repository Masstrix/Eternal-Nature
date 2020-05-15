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
import me.masstrix.eternalnature.util.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.io.*;
import java.util.*;

@Deprecated
public class ChunkData implements EternalChunk {

    private static final int VERSION = 2;
    private static final int sections = 16;
    private static final int sectionVolume = 4096;

    private Set<WaterfallEmitter> waterfallEmitters = new HashSet<>();
    private Set<EVector> smokeEmitter = new HashSet<>();
    private WorldData worldData;
    private SystemConfig config;
    private int x;
    private int z;
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
        World world = worldData.asBukkit();
        smokeEmitter.forEach(v -> {
            if (MathUtil.randomInt(10) == 2) {
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, v.toLocation(world), 0, 0, 1, 0, 0.1);
            }
        });
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
     * Creates a smoke emitter.
     *
     * @param block block to emit from.
     */
    void createSmokeEmitter(Block block) {
        if (block.getType() != Material.LAVA) return;
        Levelled data = (Levelled) block.getBlockData();
        if (data.getLevel() == 6) {
            smokeEmitter.add(new EVector(block.getX() + 0.5, block.getY(), block.getZ() + 0.5));
        }
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

    /**
     * Saves the chunks data to file.
     */
    public void saveToFile() {
        if (!worldData.plugin.getSystemConfig().isEnabled(ConfigOption.TEMPERATURE_SAVE_DATA)) return;
        File worldsFile = new File(worldData.plugin.getDataFolder(), "worlds");
        File worldFile = new File(worldsFile, worldData.getWorldName());
        File chunkFile = new File(worldFile, WorldData.pair(x, z) + ".dat");
        if (!worldFile.exists())
            worldFile.mkdirs();
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(chunkFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeInt(VERSION);
            out.writeObject(waterfallEmitters);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOut != null) {
                try {
                    fileOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int getSection(double y) {
        int section = ((int) Math.floor(y)) / 16;
        return section > sections ? sections : section < 0 ? 0 : section;
    }
}
