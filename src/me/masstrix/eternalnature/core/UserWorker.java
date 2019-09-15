package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.EternalEngine;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class UserWorker implements EternalWorker {

    private EternalNature plugin;
    private EternalEngine engine;
    private BukkitTask task;

    public UserWorker(EternalNature plugin, EternalEngine engine) {
        this.plugin = plugin;
        this.engine = engine;
    }

    @Override
    public void start() {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                engine.getCashedUsers().forEach(UserData::tick);
            }
        }.runTaskTimer(plugin, 0, 10);
    }

    @Override
    public void end() {
        if (task != null) task.cancel();

        // End everyone's session and save it.
        for (UserData user : engine.getCashedUsers()) {
            user.endSession();
        }
    }
}
