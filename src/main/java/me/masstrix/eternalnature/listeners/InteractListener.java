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
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.player.UserData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.List;

public class InteractListener implements Listener, Configurable {

    private final EternalEngine ENGINE;
    private boolean canDrinkFromOcean;

    public InteractListener(EternalNature plugin) {
        this.ENGINE = plugin.getEngine();
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        canDrinkFromOcean = section.getBoolean("hydration.drink-from-open-water");
    }

    @EventHandler
    public void onDrink(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Don't do any checks if drinking from open water is disabled.
        if (!canDrinkFromOcean)
            return;

        // If the player is underwater stop them from drinking it.
        // If the player is swimming on the surface they can still drink from
        // the water.
        if (player.getLocation().clone().add(0, 1, 0)
                .getBlock().getType() == Material.WATER) {
            return;
        }
        // Don't interact with water if they are hitting.
        if (!event.getAction().name().contains(player.getMainHand().name())) {
            return;
        }

        // Ignore other hand otherwise we get greedy and sip twice.
        if (event.getHand() != EquipmentSlot.OFF_HAND) return;


        // Don't drink if they are swimming! That's called drowning.
        if (player.getEyeLocation().getBlock().getType() == Material.WATER) {
            return;
        }

        // Dont drink if the player is looking at an entity.
        List<Entity> entities = player.getNearbyEntities(4, 4, 4);
        for (Entity e : entities) {
            Location eye = player.getEyeLocation();
            Vector toEntity = e.getLocation().toVector().subtract(eye.toVector());
            double dot = toEntity.normalize().dot(eye.getDirection());

            if (dot > 0.95D)
                return;
        }

        Location origin = player.getEyeLocation().clone();
        Vector direction = origin.getDirection().clone().normalize();

        // Shoot ray in players direction to check if a block of water is in
        // line of sight.
        for (int i = 0; i < 3; i++) {
            origin.add(direction);
            Block block = origin.getBlock();

            // We can't quite sip through walls.
            if (!block.isPassable()) {
                break;
            }
            if (block.isLiquid() && block.getType() == Material.WATER) {
                UserData data = ENGINE.getUserData(player.getUniqueId());
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
