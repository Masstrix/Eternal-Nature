package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.EternalWorker;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldProvider implements EternalWorker {

    private EternalNature plugin;
    private Map<UUID, WorldData> worldData = new HashMap<>();
    private BukkitTask ticker;
    private int tick = 0;

    public WorldProvider(EternalNature plugin) {
        this.plugin = plugin;
    }

    public WorldData getWorld(World world) {
        return getWorld(world.getUID());
    }

    /**
     * @param uuid name of the world. Case sensitive.
     * @return the worlds data.
     */
    public WorldData getWorld(UUID uuid) {
        if (worldData.containsKey(uuid)) {
            return worldData.get(uuid);
        }

        WorldData data = new WorldData(plugin, uuid);
        worldData.put(uuid, data);
        return data;
    }

    @Override
    public void start() {
        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                if (tick++ == 10) {
                    tick = 0;
                    worldData.forEach((n, w) -> w.tick());
                }
                worldData.forEach((n, w) -> w.render());
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @Override
    public void end() {
        ChunkData.killProcesses();
        ticker.cancel();
        worldData.forEach((n, w) -> w.unload());
    }
}
