package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private EternalNature plugin;

    public ConnectionListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getEngine().loadPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getEngine().unloadUserData(event.getPlayer().getUniqueId());
    }
}
