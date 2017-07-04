package com.astronstudios.natrual.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.*;

public class DeathListener implements Listener {

    private static Map<UUID, String> reasons = new HashMap<>();

    @EventHandler
    public void on(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.spigot().respawn();

        if (reasons.containsKey(player.getUniqueId())) {
            event.setDeathMessage(String.format(reasons.get(player.getUniqueId()), player.getName()));
        }
    }

    public static void kill(Player player, String reason) {
        reasons.put(player.getUniqueId(), reason);
        player.setHealth(0D);
        player.spigot().respawn();
    }
}
