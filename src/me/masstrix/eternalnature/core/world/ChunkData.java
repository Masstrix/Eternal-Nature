package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.util.CuboidScanner;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.SimpleThreadFactory;
import me.masstrix.eternalnature.util.Stopwatch;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkData {

    private static ExecutorService threadPool = Executors.newFixedThreadPool(20,
            new SimpleThreadFactory("ChunkWorker"));

    private static final int sections = 16;
    private static final int sectionVolume = 4096;
    private Map<Vector, Float> temp = new ConcurrentHashMap<>();
    private Set<WaterfallEmitter> waterfallEmitters = new HashSet<>();
    private Set<Vector> smokeEmitter = new HashSet<>();
    private boolean[] sectionsLoaded = new boolean[sections];
    private WorldData worldData;
    private int x;
    private int z;
    private long key;

    public ChunkData(WorldData worldData, int x, int z) {
        this.worldData = worldData;
        this.x = x;
        this.z = z;
        this.key = WorldData.pair(x, z);

        worldData.plugin.getLogger().info("Loaded chunk " + x + ", " + z);
    }

    void setValue(Vector pos, float val) {
        this.temp.put(pos, val);
    }

    /**
     * Kills all currently active worker threads.
     */
    static void killProcesses() {
        threadPool.shutdownNow();
    }

    /**
     * Renders all particles and effects within this chunk.
     */
    void render() {
        if (worldData.plugin.getSystemConfig().areWaterfallsEnabled() && waterfallEmitters.size() > 0) {
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

    public Set<WaterfallEmitter> getWaterfallEmitters() {
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

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public long getKey() {
        return key;
    }

    public void load() {

    }

    /**
     * Generates a section of the chunks data. Rather than generating the entire chunk
     * each chunk is generated in sections of 16 stopping any un-needed data being
     * generated.
     *
     * @param section section being generated. Range of 0-15.
     */
    public void calculateSection(final int section) {
        if (section >= sections || section < 0) return; // Invalid height
        if (sectionsLoaded[section]) return; // Ignore if data loaded
        sectionsLoaded[section] = true;

        World world = worldData.asBukkit();
        if (world == null) return; // World not loaded
        final Chunk chunk = world.getChunkAt(x, z);

        final TemperatureData dataMap = worldData.plugin.getEngine().getTemperatureData();

        // Run process on new thread
        threadPool.execute(() -> {
            Stopwatch time = new Stopwatch().start();
            Map<Vector, Float> blockData = new HashMap<>();

            int x = 0, y = section * 16, z = 0;
            for (int i = 0; i < sectionVolume; i++) {

                Vector pos = new Vector(x, y, z);

                Block block = chunk.getBlock(x, y, z);

                final float biomeTemp = dataMap.getEmissionValue(
                        TemperatureData.DataTempType.BIOME,
                        block.getBiome().name());

                this.temp.put(pos, biomeTemp);

                createWaterfallEmitter(block);
                createSmokeEmitter(block);

                float emissionTemp = dataMap.getEmissionValue(
                        TemperatureData.DataTempType.BLOCK,
                        block.getType().name());

                if (emissionTemp != 0) {
                    blockData.put(pos, emissionTemp);
                }

                x++;
                if (x >= 16) {
                    x = 0;
                    z++;
                }
                if (z >= 16) {
                    z = 0;
                    y++;
                }
            }

            worldData.plugin.getLogger().info("Generated section " + section + " in chunk "
                    + this.x + ", " + this.z + " in " + time.stop() + "ms");
            time.start();
            smooth(blockData);
        });
    }

    private void smooth(final Map<Vector, Float> data) {
        // Smooth emission values
        for (Map.Entry<Vector, Float> entry : data.entrySet()) {
            int xx = entry.getKey().getBlockX();
            int yy = entry.getKey().getBlockY();
            int zz = entry.getKey().getBlockZ();
            Vector center = new Vector(xx, yy, zz);

            int falloff = 7;
            float hotPoint = entry.getValue() / 2;

            new CuboidScanner(falloff, xx, yy, zz, (CuboidScanner.CuboidTask) (x, y, z) -> {
                Vector v = new Vector(x, y, z);

                int chunkX = worldData.asChunk(x);
                int chunkZ = worldData.asChunk(z);

                ChunkData chunk = this;

                if (chunkX != this.x || chunkZ != this.z) {
                    chunk = worldData.getChunk(chunkX, chunkZ);
                }

                chunk.temp.compute(v, (vec, t) -> {
                    int distance = (int) Math.ceil(center.distance(v));
                    double fallOffPercent = ((double) (falloff - distance)) / (double) falloff;
                    float point = (float) (hotPoint * fallOffPercent);

                    if (t == null || t < point) return point;
                    return t;
                });
            }).excludeCenter().start();

            temp.computeIfPresent(entry.getKey(), (vec, t) -> t + hotPoint);
        }
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
            smokeEmitter.add(new Vector(block.getX() + 0.5, block.getY(), block.getZ() + 0.5));
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

    public void unloadSection(int section) {
        if (section > sections || section < 0) return; // Invalid value
        if (!sectionsLoaded[section]) return;
    }

    public boolean hasTemperatureData(Vector point) {
        return temp.containsKey(floor(point));
    }

    public float getTemperature(Vector point) {
        return temp.getOrDefault(point, Float.NEGATIVE_INFINITY);
    }

    public static Vector floor(Vector vector) {
        return floor(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector floor(double x, double y, double z) {
        return new Vector(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    public static int getSection(double y) {
        int section = ((int) Math.floor(y)) / 16;
        return section > sections ? sections : section < 0 ? 0 : section;
    }
}
