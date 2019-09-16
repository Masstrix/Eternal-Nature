package me.masstrix.eternalnature.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathListener implements Listener {

    private static Map<UUID, String> customReasons = new HashMap<>();

    /**
     * Adds a custom reason for why a player was killed. This is used as their death
     * message if the last damage they took was defined as a custom cause.
     *
     * @param player player to assign this to.
     * @param message message to send when they are killed.
     */
    public static void logCustomReason(Player player, String message) {
        customReasons.put(player.getUniqueId(), message);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.CUSTOM) {
            if (customReasons.containsKey(player.getUniqueId())) {
                event.setDeathMessage(customReasons.get(player.getUniqueId()));
            }
        }
        // Remove any instance of the player
        customReasons.remove(player.getUniqueId());
    }
}
