package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.api.EternalWorld;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.util.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
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
    private TemperatureData temperatureData;

    public WorldData(EternalNature plugin, UUID world) {
        this.plugin = plugin;
        this.world = world;
        temperatureData = plugin.getEngine().getTemperatureData();
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
            return temperatureData.getExactBiomeTemp(biome);
        }
        return Double.NEGATIVE_INFINITY;
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
