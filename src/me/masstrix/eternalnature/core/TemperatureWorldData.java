package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.util.SimpleThreadFactory;
import me.masstrix.eternalnature.util.Stopwatch;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Deprecated
public class TemperatureWorldData {

    private EternalNature plugin;
    private TemperatureWorker worker;
    private TemperatureData dataMap;
    private Map<Vector, Float> dataPoint = new ConcurrentHashMap<>();
    private List<BoundingBox> working = new ArrayList<>();

    private static int scanRadius = -1;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(20, new SimpleThreadFactory("WorldDataWorker"));

    public TemperatureWorldData(EternalNature plugin, TemperatureWorker worker) {
        this.plugin = plugin;
        this.worker = worker;
        this.dataMap = worker.getDataMap();

        if (scanRadius == -1)
            scanRadius = plugin.getSystemConfig().getScanRadius();
    }

    void endAll() {
        threadPool.shutdownNow();
    }

    /**
     * Calculates the temperature of an area from a center point.
     *
     * @param location center point of where to scan from.
     */
    public void calculateArea(Location location) {
        int rad = 10;
        System.out.println(" ");
        System.out.println("Calculating new area...");
        Stopwatch s = new Stopwatch().start();

        final Location center = location.clone().getBlock().getLocation();

        Vector min = center.clone().add(-rad, -rad, -rad).toVector();
        Vector max = center.clone().add(rad, rad, rad).toVector();
        BoundingBox area = new BoundingBox(min, max);

        // Stop calculation if it overlaps a current process.
        for (BoundingBox box : working) {
            if (box.overlaps(area)) return;
        }
        working.add(area);

        Map<Vector, Float> blockData = new HashMap<>();

        int scanned = 0, added = 0;

        // Generate the map
        for (int y = -rad; y <= rad; y++) {
            for (int x = -rad; x <= rad; x++) {
                for (int z = -rad; z <= rad; z++) {
                    Location loc = center.clone().add(x, y, z);
                    Block block = loc.getBlock();
                    final float value = dataMap.getEmissionValue(
                            TemperatureData.DataTempType.BIOME,
                            block.getBiome().name());
                    float emission = dataMap.getEmissionValue(
                            TemperatureData.DataTempType.BLOCK,
                            block.getType().name());

                    if (emission != 0) {
                        blockData.put(loc.toVector(), emission);
                        added++;
                    }
                    if (dataPoint.containsKey(loc.toVector())) {
                        dataPoint.computeIfPresent(loc.toVector(), (vec, f) -> value > f ? value : f);
                    } else {
                        dataPoint.put(loc.toVector(), value);
                    }
                    scanned++;
                }
            }
        }

        System.out.println("Loaded Exact in " + s.stop() + "ms");
        dataPoint.putAll(blockData);
        working.remove(area);


        if (blockData.size() > 0)
            smoothBlockData(area, blockData);

        System.out.println("Generated in " + s.stop() + "ms (" + scanned + " scanned, " + added + " processed" + ")");
        System.out.println(" ");
    }

    /**
     * Gradate extreme points to have falloffs for their temperature.
     *
     * @param area area the smooth is working in.
     * @param blocks map of all blocks with emission values.
     */
    private void smoothBlockData(final BoundingBox area, final Map<Vector, Float> blocks) {
        threadPool.submit(() -> {
            Stopwatch s = new Stopwatch().start();
            int operations = 0;
            for (Map.Entry<Vector, Float> entry : blocks.entrySet()) {
                int rad = 5;

                for (int y = -rad; y <= rad; y++) {
                    for (int x = -rad; x <= rad; x++) {
                        for (int z = -rad; z <= rad; z++) {

                            Vector pos = new Vector(x, y, z).add(entry.getKey());
                            double distance = pos.distance(entry.getKey()) + 1;
                            final float value = entry.getValue() / ((float) distance);

                            if (!dataPoint.containsKey(pos) || dataPoint.get(pos) < value) {
                                dataPoint.put(pos, value);
                            }
                            operations++;
                        }
                    }
                }
            }

            working.remove(area);
            System.out.println("Completed " + operations + " smoothing operations in " + s.stop() + "ms");
        });
    }

    /**
     * Blurs data point to average out the temperature along a radius.
     *
     * @param pixel pixel point position being blurred.
     * @param rad radius to scan to blur.
     * @return the pixel as an averaged value against surrounding values.
     */
    private float blurPixel(Vector pixel, int rad) {
        float total = 0;
        int count = 0;
        for (int y = -rad; y <= rad; y++) {
            for (int x = -rad; x <= rad; x++) {
                for (int z = -rad; z <= rad; z++) {
                    Vector pos = pixel.clone().add(new Vector(x, y, z));
                    total += getTemperatureAtOr(pos, 0);
                    count++;
                }
            }
        }
        return total / count;
    }

    /**
     * Unloads all data from a chunk.
     *
     * TODO save chunk data to file.
     *
     * @param x x position of chunk.
     * @param z z position of chunk.
     */
    void unloadChunk(int x, int z) {
        Stopwatch s = new Stopwatch().start();
        threadPool.execute(() -> {
            int operations = 0;
            int y = 0, cx = 0, cz = 0;
            for (int i = 0; i < (16 * 16 * 255); i++) {
                Vector pos = new Vector(cx + (x * 16), y, cz + (z * 16));
                if (dataPoint.containsKey(pos)) {
                    operations++;
                    dataPoint.remove(new Vector(cx + (x * 16), y, cz + (z * 16)));
                }
                cx++;
                if (cx == 16) {
                    cx = 0;
                    cz++;
                    if (cz == 16) {
                        cz = 0;
                        y++;
                    }
                }
            }
            if (operations > 0)
                System.out.println("Removed chunk data in " + s.stop() + "ms   -   " + operations);
        });
    }

    public Map<Vector, Float> getPointArea(BoundingBox area) {
        Map<Vector, Float> points = new HashMap<>();
        for (double y = area.getMin().getY(); y < area.getMax().getY(); y++) {
            for (double x = area.getMin().getX(); x < area.getMax().getX(); x++) {
                for (double z = area.getMin().getZ(); z < area.getMax().getZ(); z++) {
                    Vector v = new Vector(x, y, z);
                    points.put(v, dataPoint.getOrDefault(v, -9999F));
                }
            }
        }
        return points;
    }

    public void setPoint(Vector pos, float temp) {
        dataPoint.put(pos, temp);
    }

    public float getTemperatureAt(Vector pos) {
        if (dataPoint.containsKey(pos))
            return dataPoint.get(pos);
        return Float.NEGATIVE_INFINITY;
    }

    public float getTemperatureAtOr(Vector pos, float zero) {
        if (dataPoint.containsKey(pos))
            return dataPoint.get(pos);
        return zero;
    }

    public Map<Vector, Float> getDataPoints() {
        return dataPoint;
    }
}
