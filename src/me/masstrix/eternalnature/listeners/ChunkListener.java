package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.world.WorldData;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkListener implements Listener {

    private EternalNature plugin;

    public ChunkListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(ChunkUnloadEvent event) {
        int x = event.getChunk().getX();
        int z = event.getChunk().getZ();

        World world = event.getWorld();
        WorldData data = plugin.getEngine().getWorldProvider().getWorld(world);
        data.unloadChunk(x, z);
    }
}
