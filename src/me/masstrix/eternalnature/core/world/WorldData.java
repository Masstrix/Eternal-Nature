package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.api.EternalWorld;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldData implements EternalWorld {

    private Map<Long, ChunkData> chunks = new HashMap<>();
    private String worldName;
    protected EternalNature plugin;
    private UUID world;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(20,
            new SimpleThreadFactory("ChunkWorker"));
    private List<UUID> computing = new ArrayList<>(); // List of all tasks currently executing.

    public WorldData(EternalNature plugin, UUID world) {
        this.plugin = plugin;
        this.world = world;
    }

    public UUID getWorldUid() {
        return world;
    }

    public void tick() {
        chunks.forEach((l, c) -> c.tick());
    }

    public void render() {
        chunks.forEach((l, c) -> c.render());
    }

    public void unload() {

    }

    public World asBukkit() {
        return Bukkit.getWorld(world);
    }

    public void loadNearby(Vector vec) {
        loadNearby((int) Math.floor(vec.getX()), (int) Math.floor(vec.getY()), (int) Math.floor(vec.getZ()));
    }

    public int getChunksLoaded() {
        return chunks.size();
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
        int range = 1;
        x /= 16;
        y /= 16;
        z /= 16;
        x -= 1;

        int cx = -range, cy = -range, cz = -range;
        for (int i = 0; i < MathUtil.cube(3); i++) {

            int sectionY = cy + y;
            if (sectionY > 16 || sectionY < 0) continue; // Can blocks be placed

            ChunkData chunk = getChunk(cx + x, cz + z);
            if (chunk == null) {
                chunk = loadChunk(cx + x, cz + z);
            }

            chunk.calculateSection(sectionY);

            cx++;
            if (cx > range) {
                cx = -range;
                cz++;
                if (cz > range) {
                    cz = -range;
                    cy++;
                }
            }
        }
    }

//    public void calculateArea(final UUID task, final int posX, final int posY, final int posZ) {
//        if (computing.contains(task)) return; // Stop if task is already computing
//        computing.add(task);
//        World world = asBukkit();
//        if (world == null) return; // World not loaded
//
//        final int rad = 8;
//
//        final TemperatureData dataMap = plugin.getEngine().getTemperatureData();
//
//        threadPool.execute(() -> {
//            Stopwatch time = new Stopwatch().start();
//            Map<Vector, Float> blocks = new HashMap<>();
//
//            new CuboidScanner(rad, posX, posY, posZ, (CuboidScanner.CuboidLocalTask)
//                    (x, y, z, localX, localY, localZ) -> {
//
//                        // Local chunk position of block
//                        Vector pos = new Vector(inChunk(x), y, inChunk(z));
//
//                        Block block = world.getBlockAt(x, y, z);
//
//                        final float biomeTemp = dataMap.getEmissionValue(
//                                TemperatureData.DataTempType.BIOME,
//                                block.getBiome().name());
//
//                        ChunkData chunk = getChunkFromPosition(x, z);
//                        chunk.setValue(pos, biomeTemp);
//
//                        // Check if water for waterfall
//                        chunk.createWaterfallEmitter(block);
//                        chunk.createSmokeEmitter(block);
//
//                        float emissionTemp = dataMap.getEmissionValue(
//                                TemperatureData.DataTempType.BLOCK,
//                                block.getType().name());
//
//                        //temp.put(pos, biomeTemp);
//                        if (emissionTemp != 0) {
//                            blocks.put(pos, emissionTemp);
//                        }
//            }).start();
//
//            // Smooth block values
//            for (Map.Entry<Vector, Float> entry : blocks.entrySet()) {
//                int xx = entry.getKey().getBlockX();
//                int yy = entry.getKey().getBlockY();
//                int zz = entry.getKey().getBlockZ();
//                Vector center = new Vector(xx, yy, zz);
//
//                float hotPoint = entry.getValue() / 2;
//
//                new CuboidScanner(rad, xx, yy, zz, (CuboidScanner.CuboidTask) (x, y, z) -> {
//                    Vector v = new Vector(x, y, z);
//                    ChunkData chunk = getChunkFromPosition(x, z);
//
//                    int distance = (int) Math.ceil(center.distance(v));
//                    double fallOffPercent = ((double) (rad - distance)) / (double) rad;
//                    float point = (float) (hotPoint * fallOffPercent);
//
//                    if (chunk.getTemperature(v) < point)
//                        chunk.setValue(v, point);
//                }).excludeCenter().start();
//            }
//
//            computing.remove(task);
//            plugin.getLogger().info("Generated area " + Arrays.toString(new int[] {posX, posY, posZ})
//                    + " in " + time.stop() + "ms");
//        });
//    }

    int inChunk(int v) {
        int val = v % 16;
        return v < 0 ? 16 - -val : val;
    }

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
        if (chunks.containsKey(pair)) return chunks.get(pair);
        ChunkData chunkData = null;
        File worldsFile = new File(plugin.getDataFolder(), "worlds");
        File worldFile = new File(worldsFile, getWorldUid().toString());
        File chunkFile = new File(worldFile, WorldData.pair(x, z) + ".dat");
        if (chunkFile.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(chunkFile);
                chunkData = new ChunkData(this, x, z, in.readAllBytes());
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
        if (valid != null)
            plugin.getLogger().info("Unloaded chunk " + x + ", " + z + " in world " + worldName);
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
