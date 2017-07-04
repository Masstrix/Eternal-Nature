package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.NaturalEnvironment;
import com.astronstudios.natrual.util.CustomStack;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class WalkListener implements Listener {

    @EventHandler
    public void on(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().distance(event.getFrom()) > 0 && !player.isInsideVehicle()) {
            NaturalEnvironment.getInstance().get(player.getUniqueId()).addDistance(event.getTo().distance(event.getFrom()));
        }
    }
}
