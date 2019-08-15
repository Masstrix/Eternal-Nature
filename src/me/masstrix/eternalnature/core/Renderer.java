package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.EternalEngine;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Renderer implements EternalWorker {

    private EternalNature plugin;
    private EternalEngine engine;
    private BukkitTask renderTask;

    public Renderer(EternalNature plugin, EternalEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    @Override
    public void start() {
        renderTask = new BukkitRunnable() {
            @Override
            public void run() {
                render();
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    /**
     * Render all components in the plugin.
     */
    private void render() {
        for (UserData user : engine.getCashedUsers()) {
            if (!user.isOnline()) continue;
            user.render();
        }
    }

    @Override
    public void end() {
        if (renderTask != null)
            renderTask.cancel();
    }
}
