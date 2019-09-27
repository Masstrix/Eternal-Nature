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
import me.masstrix.eternalnature.api.EternalWorld;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.util.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldData implements EternalWorld {

    private Map<Long, ChunkData> chunks = new HashMap<>();
    private String worldName;
    protected EternalNature plugin;
    private UUID world;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(20,
            new SimpleThreadFactory("ChunkWorker"));
    private TemperatureData temperatureData;
    Map<Position, WaterfallEmitter> waterfalls = new ConcurrentHashMap<>();

    public WorldData(EternalNature plugin, UUID world) {
        this.plugin = plugin;
        this.world = world;
        temperatureData = plugin.getEngine().getTemperatureData();
        worldName = asBukkit().getName();
        loadData();

        int delay = 20 * 20 * 5;
        new BukkitRunnable() {
            @Override
            public void run() {
                //saveData();
            }
        }.runTaskTimerAsynchronously(plugin, delay, delay);
    }

    public UUID getWorldUid() {
        return world;
    }

    public void tick() {
        /*
        if (plugin.getSystemConfig().isEnabled(ConfigOption.WATERFALLS)) {
            List<WaterfallEmitter> bin = new ArrayList<>();
            for (WaterfallEmitter waterfall : waterfalls.values()) {
                if (!waterfall.isValid()) {
                    bin.add(waterfall);
                    continue;
                }
                waterfall.tick();
            }
            bin.forEach(fall -> waterfalls.remove(fall.pos));
        }
        */
        chunks.forEach((l, c) -> c.tick());
    }

    public void render() {
        chunks.forEach((l, c) -> c.render());
    }

    public void unload() {
        saveData();
    }

    public void saveData() {
        File worlds = new File(plugin.getDataFolder(), "worlds");
        File data = new File(worlds, worldName + ".etw");
        if (!worlds.exists())
            worlds.mkdirs();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(data));
            writer.write("[waterfalls]");
            writer.write(Character.LINE_SEPARATOR);
            for (WaterfallEmitter waterfall : waterfalls.values()) {
                writer.write("  ");
                writer.write(waterfall.serialize());
                writer.write(Character.LINE_SEPARATOR);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        WorldData worldData = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                File worlds = new File(plugin.getDataFolder(), "worlds");
                File data = new File(worlds, worldName + ".etw");
                if (!data.exists()) return;
                try {
                    String header = "";
                    int waterfallsLoaded = 0;
                    for (String s : Files.readAllLines(data.toPath())) {
                        if (s.startsWith("[") && s.endsWith("]")) {
                            header = s;
                            continue;
                        }
                        if (header.equals("[waterfalls]")) {
                            WaterfallEmitter emitter = WaterfallEmitter.deserialize(worldData,
                                    s.replaceFirst(" {2}", ""));
                            if (emitter != null) {
                                waterfalls.put(emitter.pos, emitter);
                                waterfallsLoaded++;
                            }
                        }
                    }
                    plugin.getLogger().info("Loaded " + waterfallsLoaded + " waterfalls");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void createWaterfall(Location loc) {
//        Block block = loc.getBlock();
//        Position pos = new Position(block.getX(), block.getY(), block.getZ());
//        WaterfallEmitter emitter = new WaterfallEmitter(loc);
//        waterfalls.put(pos, emitter);
    }

    public World asBukkit() {
        return Bukkit.getWorld(world);
    }

    @Deprecated
    public void loadNearby(Vector vec) {
        loadNearby((int) Math.floor(vec.getX()), (int) Math.floor(vec.getY()), (int) Math.floor(vec.getZ()));
    }

    public int getChunksLoaded() {
        return chunks.size();
    }

    /**
     * Returns the biome temperature for a block.
     *
     * @param x x block position.
     * @param y y block position.
     * @param z z block position.
     * @return the blocks biome temperature or <i>NEGATIVE_INFINITY</i> if there was
     *         an error.
     */
    public double getBiomeTemperature(int x, int y, int z) {
        World world = Bukkit.getWorld(this.world);
        if (world != null) {
            Biome biome = world.getBlockAt(x, y, z).getBiome();
            return temperatureData.getBiomeModifier(biome);
        }
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * Scans around the location and includes the center for the average
     * temperate value for surrounding biomes.
     *
     * @param x x block position.
     * @param y y block position.
     * @param z z block position.
     * @return the ambient temperature.
     */
    public double getAverageAmbientTemp(int x, int y, int z) {
        double total = getBiomeTemperature(x, y, z);
        int amount = 5;
        int radius = 15;
        double increment = (2 * Math.PI) / amount;
        for (int i = 0; i < amount; i++) {
            double angle = i * increment;
            int blockX = (int) (x + (radius * Math.cos(angle)));
            int blockZ = (int) (z + (radius * Math.sin(angle)));

            double temp = getBiomeTemperature(blockX, y, blockZ);
            total += temp;
        }
        return total / (amount + 1);
    }

    /**
     * Returns the current temperature of a block. This will vary depending on the biome,
     * sky light.
     *
     * @param x x block position.
     * @param y y block position.
     * @param z z block position.
     * @return the blocks temperature or <i>INFINITY</i> if there was an error.
     */
    public double getBlockTemperature(int x, int y, int z) {
        World world = Bukkit.getWorld(this.world);
        if (world == null) return Double.NEGATIVE_INFINITY;
        Block block = world.getBlockAt(x, y, z);
        double temp = getAverageAmbientTemp(x, y, z);

        // Apply modifier if block has sunlight.
        if (block.getLightFromSky() > 0) {
            double directSunAmplifier = temperatureData.getDirectSunAmplifier() - 1;
            byte skyLight = block.getLightFromSky();
            double percent = skyLight / 15D;
            temp *= directSunAmplifier * percent + 1;
        }

        // Apply modifier if block is in a "cave"
        if (((block.getLightFromSky() <= 6 && block.getLightLevel() < 6)
                || block.getType() == Material.CAVE_AIR)
                && block.getLightLevel() != 15) {
            double amp = temperatureData.getCaveModifier() - 1;
            byte light = block.getLightLevel();
            double percent = (15D - light) / 15D;
            temp *= amp * percent + 1;
        }
        return temp;
    }

    public double getTemperature(int x, int y, int z) {
        return getTemperature(new Vector(x, y, z));
    }

    public float getTemperature(Vector loc) {
        ChunkData chunk = getChunkFromPosition(loc.getBlockX(), loc.getBlockZ());
        return chunk != null ? chunk.getTemperature(new EVector(inChunk(loc.getBlockX()), loc.getBlockY(), inChunk(loc.getBlockZ()))) : 0;
    }

    /**
     * Loads all surrounding chunk sections and chunks around the given cords. All
     * chunks are loaded in a 3 cubed volume.
     *
     * @param x x position to load from.
     * @param y y position to load from.
     * @param z z position to load from.
     */
    @Deprecated
    public void loadNearby(int x, int y, int z) {
        x /= 16;
        y /= 16;
        z /= 16;
        x -= 1;

        ChunkData chunk = getChunk(x, z);
        if (chunk == null) chunk = loadChunk(x, z);
        //chunk.calculateSection(y);

//        new CuboidScanner(1, x, y, z, (CuboidScanner.CuboidTask) (x1, section, z1) -> {
//            if (section > 16 || section < 0) return;
//
//            ChunkData chunk = getChunk(x1, z1);
//            if (chunk == null) chunk = loadChunk(x1, z1);
//
//            chunk.calculateSection(section);
//        }).start();
    }

    /**
     * @param v value to round.
     * @return the position rounded to a chunks local coordinates.
     */
    int inChunk(int v) {
        int val = v % 16;
        return v < 0 ? 16 - -val : val;
    }

    /**
     * @param v value to round.
     * @return the position of a world location as a chunks location.
     */
    int asChunk(int v) {
        int val = v / 16;
        if (v < 0) val -= 1;
        return val;
    }

    public ChunkData getChunkFromPosition(int x, int z) {
        int chunkX = x / 16;
        int chunkZ = z / 16;
        if (x < 0) chunkX -= 1;
        if (z < 0) chunkZ -= 1;
        return loadChunk(chunkX, chunkZ);
    }

    /**
     * @param x x position of chunk.
     * @param z z position of chunk.
     * @return null if no chunk data has been generated otherwise return
     *         the chunk.
     */
    public ChunkData getChunk(int x, int z) {
        long pair = pair(x, z);
        if (isChunkLoaded(x, z)) return chunks.get(pair);
        ChunkData chunkData = null;
        File worldsFile = new File(plugin.getDataFolder(), "worlds");
        File worldFile = new File(worldsFile, getWorldUid().toString());
        File chunkFile = new File(worldFile, WorldData.pair(x, z) + ".dat");
        if (chunkFile.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(chunkFile);
                //chunkData = new ChunkData(this, x, z, in.readAllBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (chunkData == null) chunkData = new ChunkData(this, x, z);
            chunks.put(pair, chunkData);
        }
        return chunkData;
    }

    /**
     * Loads a chunk.
     *
     * @param x x position of chunk.
     * @param z z position of chunk.
     */
    public ChunkData loadChunk(int x, int z) {
        long key = pair(x, z);
        ChunkData data = getChunk(x, z);
        if (data != null) return chunks.get(key);
        data = new ChunkData(this, x, z);
        chunks.put(key, data);
        return data;
    }

    /**
     * Unloads a chunk from cache.
     *
     * @param x x position of chunk to unload.
     * @param z z position of chunk to unload.
     */
    public void unloadChunk(int x, int z) {
        long pair = pair(x, z);
        Object valid = chunks.remove(pair);
    }

    public boolean isChunkLoaded(int x, int z) {
        return chunks.containsKey(pair(x, z));
    }

    public static long pair(int var0, int var1) {
        return (long) var0 & 4294967295L | ((long) var1 & 4294967295L) << 32;
    }

    public static int getX(long var0) {
        return (int) (var0 & 4294967295L);
    }

    public static int getZ(long var0) {
        return (int) (var0 >>> 32 & 4294967295L);
    }
}
