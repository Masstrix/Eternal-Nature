package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.world.ChunkData;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private EternalNature plugin;

    public MoveListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        Player player = event.getPlayer();
        double distance = from.distanceSquared(to);
        if (distance == 0) return;

        // Does the players chunk section change
        if (from.getChunk() != to.getChunk() || ChunkData.getSection(from.getY())
                != ChunkData.getSection(to.getY())) {
            // Load nearby sections
            WorldProvider provider = plugin.getEngine().getWorldProvider();
            WorldData worldData = provider.getWorld(player.getWorld());
            if (worldData != null) {
                worldData.loadNearby(to.toVector());
            }
        }

        // Ignore movement if player is a passenger.
        if (!player.isInsideVehicle() && !player.isGliding() && !player.isRiptiding()) {
            UserData user = plugin.getEngine().getUserData(player.getUniqueId());
            if (user != null) user.addWalkDistance((float) distance, player.isSprinting());
        }
    }
}
