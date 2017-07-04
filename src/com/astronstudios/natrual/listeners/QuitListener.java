package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.NaturalEnvironment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void on(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        NaturalEnvironment.getInstance().remove(player.getUniqueId());
    }
}
