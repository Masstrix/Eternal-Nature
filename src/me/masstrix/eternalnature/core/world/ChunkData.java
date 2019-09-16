package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.api.EternalChunk;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.util.*;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkData implements EternalChunk {

    private static final int VERSION = 1;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(20,
            new SimpleThreadFactory("ChunkWorker"));
    private static final int sections = 16;
    private static final int sectionVolume = 4096;

    private Map<EVector, Float> temp = new ConcurrentHashMap<>();
    private Set<WaterfallEmitter> waterfallEmitters = new HashSet<>();
    private Set<EVector> smokeEmitter = new HashSet<>();
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
            char[] loaded = in.readUTF().toCharArray();
            for (int i = 0; i < loaded.length; i++) {
                sectionsLoaded[i] = loaded[i] == '1';
            }
            //noinspection unchecked
            temp = (Map<EVector, Float>) in.readObject();
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

    void setValue(EVector pos, float val) {
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
        if (worldData.plugin.getSystemConfig().isEnabled(ConfigOption.WATERFALLS) && waterfallEmitters.size() > 0) {
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

        // Run process on new thread
        threadPool.execute(() -> {
            Stopwatch time = new Stopwatch().start();
            Map<EVector, Float> blockData = new HashMap<>();
            TemperatureData dataMap = worldData.plugin.getEngine().getTemperatureData();

            AtomicInteger operations = new AtomicInteger();

            new CuboidScanner(15, 0, section * 16, 0, (CuboidScanner.CuboidTask) (x, y, z) -> {
                EVector pos = new EVector(x, y, z);

                Block block = chunk.getBlock(x, y, z);
                //createWaterfallEmitter(block);
//                createSmokeEmitter(block);

//                final float biomeTemp = dataMap.getEmissionValue(
//                        TemperatureData.DataTempType.BIOME,
//                        block.getBiome().name());

                this.temp.put(pos, 13F);

                float emissionTemp = 13; /*dataMap.getEmissionValue(
                        TemperatureData.DataTempType.BLOCK,
                        block.getType().name());*/
                emissionTemp = dataMap.getExactBlockEmission(block.getType());

                if (emissionTemp != 0) {
                    //blockData.put(pos, emissionTemp);
                }
                operations.incrementAndGet();
            }).center(false).start();

            // Smooth the chunks data out.
            for (Map.Entry<EVector, Float> entry : blockData.entrySet()) {
                int xx = entry.getKey().getBlockX();
                int yy = entry.getKey().getBlockY();
                int zz = entry.getKey().getBlockZ();
                EVector center = new EVector(xx, yy, zz);

                int falloff = 4;
                float hotPoint = entry.getValue() / 2;

                new CuboidScanner(falloff, xx, yy, zz, (CuboidScanner.CuboidTask) (x, y, z) -> {
                    EVector v = new EVector(x, y, z);
                    operations.incrementAndGet();

                    int chunkX = worldData.asChunk(x);
                    int chunkZ = worldData.asChunk(z);

                    ChunkData local = this;

                    if (chunkX != this.x || chunkZ != this.z) {
                        local = worldData.getChunk(chunkX, chunkZ);
                    }

                    if (local != null) {
                        local.temp.compute(v, (vec, t) -> {
                            int distance = (int) Math.ceil(center.distance(v));
                            double fallOffPercent = ((double) (falloff - distance)) / (double) falloff;
                            float point = (float) (hotPoint * fallOffPercent);

                            if (t == null || t < point) return point;
                            return t;
                        });
                    }
                }).excludeCenter().start();

                temp.computeIfPresent(entry.getKey(), (vec, t) -> t + hotPoint);
            }

            //temp.putAll(blockData);
            //smooth(blockData);
            saveToFile();

            worldData.plugin.getLogger().info("Generated section " + section + " in chunk "
                    + this.x + ", " + this.z + " in " + time.stop() + "ms   (" + operations.get() + " operations)");
        });
    }

    /**
     * Smooths a map of points out for the chunks temperature. Note this task can take
     * a while to process if there are many points to be operated on so it is best to
     * try and keep smoothing to a minimum.
     *
     * @param data points being smoothed.
     */
    private void smooth(final Map<EVector, Float> data) {
        // Smooth emission values
        for (Map.Entry<EVector, Float> entry : data.entrySet()) {
            int xx = entry.getKey().getBlockX();
            int yy = entry.getKey().getBlockY();
            int zz = entry.getKey().getBlockZ();
            EVector center = new EVector(xx, yy, zz);

            int falloff = 7;
            float hotPoint = entry.getValue() / 2;

            new CuboidScanner(falloff, xx, yy, zz, (CuboidScanner.CuboidTask) (x, y, z) -> {
                EVector v = new EVector(x, y, z);

                int chunkX = worldData.asChunk(x);
                int chunkZ = worldData.asChunk(z);

                ChunkData chunk = this;

                if (chunkX != this.x || chunkZ != this.z) {
                    chunk = worldData.getChunk(chunkX, chunkZ);
                }

                if (chunk != null) {
                    chunk.temp.compute(v, (vec, t) -> {
                        int distance = (int) Math.ceil(center.distance(v));
                        double fallOffPercent = ((double) (falloff - distance)) / (double) falloff;
                        float point = (float) (hotPoint * fallOffPercent);

                        if (t == null || t < point) return point;
                        return t;
                    });
                }
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

    public void unloadSection(int section) {
        if (section > sections || section < 0) return; // Invalid value
        if (!sectionsLoaded[section]) return;
    }

    public boolean hasTemperatureData(EVector point) {
        return temp.containsKey(floor(point));
    }

    public float getTemperature(EVector point) {
        return temp.getOrDefault(point, Float.NEGATIVE_INFINITY);
    }

    public static EVector floor(EVector vector) {
        return floor(vector.getX(), vector.getY(), vector.getZ());
    }

    public static EVector floor(double x, double y, double z) {
        return new EVector(Math.floor(x), Math.floor(y), Math.floor(z));
    }

    /**
     * Saves the chunks data to file.
     */
    public void saveToFile() {
        if (!worldData.plugin.getSystemConfig().isEnabled(ConfigOption.TEMPERATURE_SAVE_DATA)) return;
        File worldsFile = new File(worldData.plugin.getDataFolder(), "worlds");
        File worldFile = new File(worldsFile, worldData.getWorldUid().toString());
        File chunkFile = new File(worldFile, WorldData.pair(x, z) + ".dat");
        if (!worldFile.exists())
            worldFile.mkdirs();
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(chunkFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeInt(VERSION);
            StringBuilder loaded = new StringBuilder();
            for (boolean b : sectionsLoaded) {
                loaded.append(b ? 1 : 0);
            }
            out.writeUTF(loaded.toString());
            out.writeObject(temp);
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
