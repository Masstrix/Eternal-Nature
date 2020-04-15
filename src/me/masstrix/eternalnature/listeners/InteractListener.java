/*
 * Copyright 2020 Matthew Denton
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

import me.masstrix.eternalnature.EternalEngine;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class InteractListener implements Listener {

    private EternalNature plugin;
    private EternalEngine engine;

    public InteractListener(EternalNature plugin) {
        this.plugin = plugin;
        this.engine = plugin.getEngine();
    }

    @EventHandler
    public void onDrink(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // If the player is underwater stop them from drinking it.
        // If the player is swimming on the surface they can still drink from
        // the water.
        if (player.getLocation().clone().add(0, 1, 0)
                .getBlock().getType() == Material.WATER) {
            return;
        }

        Location origin = player.getEyeLocation().clone();
        Vector direction = origin.getDirection().clone().normalize();

        // Shoot ray in players direction to check if a block of water is in
        // line of sight.
        for (int i = 0; i < 3; i++) {
            origin.add(direction);
            Block block = origin.getBlock();
            if (block.isLiquid() && block.getType() == Material.WATER) {
                UserData data = engine.getUserData(player.getUniqueId());
                if (!data.isHydrationFull()) {
                    data.hydrate(1);
                    data.addThirst(10);
                    origin.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1);
                    block.getWorld().spawnParticle(Particle.WATER_SPLASH, origin, 3, 0, 0, 0, 0);
                }
                break;
            }
        }
    }
}
