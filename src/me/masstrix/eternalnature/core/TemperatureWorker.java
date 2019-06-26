package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.EternalEngine;
import me.masstrix.eternalnature.EternalNature;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class TemperatureWorker implements EternalWorker {

    private EternalNature plugin;
    private EternalEngine engine;
    private TemperatureData dataMap;
    private Map<World, TemperatureWorldData> worlds = new HashMap<>();
    private OverworldHeightGradient heightGradient;

    public TemperatureWorker(EternalNature plugin, EternalEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
        dataMap = new TemperatureData(plugin);
        heightGradient = new OverworldHeightGradient();
    }

    public int getLoadedWorlds() {
        return worlds.size();
    }

    public int getCachedPoints() {
        int count = 0;
        for (TemperatureWorldData m : worlds.values())
            count += m.getDataPoints().size();
        return count;
    }

    public TemperatureData getDataMap() {
        return dataMap;
    }

    public TemperatureWorldData getWorldData(World world) {
        return worlds.get(world);
    }

    /**
     * Calculates all surrounding temperature nodes of a location.
     *
     * @param feet
     */
    public void calculateArea(final Location feet) {
        final Location loc = feet.getBlock().getLocation();
        TemperatureWorldData data = worlds.get(loc.getWorld());
        if (data == null) {
            data = new TemperatureWorldData(plugin, this);
            worlds.put(loc.getWorld(), data);
        }
        data.calculateArea(loc);
    }

    /**
     * @param loc location to get the temperature of.
     * @return the local temperature or
     */
    public float getTemperature(final Location loc) {
        return getTemperature(loc, 0);
    }

    public float getTemperature(Location feet, int range) {
        final Location loc = feet.getBlock().getLocation();
        TemperatureWorldData data = worlds.get(loc.getWorld());
        if (data == null) {
            data = new TemperatureWorldData(plugin, this);
            worlds.put(loc.getWorld(), data);
        }

        if (data.getTemperatureAt(loc.toVector()) == Float.NEGATIVE_INFINITY) {
            data.calculateArea(loc);
            return Float.NEGATIVE_INFINITY;
        }

        if (range < 1) {
            return data.getTemperatureAt(loc.toVector());
        }

        float total = 0;
        Vector base = loc.toVector();

        int cubed = range * range * range;
        int runs = 0;
        int x = 0, y = -range, z = -range;
        for (int i = 0; i <= cubed; i++) {

            Vector pos = new Vector(x, y, z).add(base);
            float val = data.getTemperatureAt(pos);

            if (val == Float.NEGATIVE_INFINITY) {
                return val;
            } else {
                total += val;
            }

            x++;
            if (x == range) {
                x = -range;
                z++;
                if (z > range) {
                    y++;
                }
            }
            runs++;
        }

        return total / runs;
    }

    public void unloadChunk(World world, int x, int z) {
        if (worlds.containsKey(world)) {
            worlds.get(world).unloadChunk(x, z);
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {
        // End all tasks
        for (TemperatureWorldData worlds : worlds.values())
            worlds.endAll();
    }
}
