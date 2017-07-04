package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.NaturalEnvironment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnListener implements Listener {

    @EventHandler
    public void on(PlayerRespawnEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                NaturalEnvironment.getInstance().get(event.getPlayer().getUniqueId()).reset();
            }
        }.runTaskLater(NaturalEnvironment.getInstance(), 5);
    }
}
