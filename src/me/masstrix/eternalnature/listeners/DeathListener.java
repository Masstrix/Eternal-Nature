/*
 * Copyright 2019 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
