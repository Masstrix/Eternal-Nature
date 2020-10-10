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

package me.masstrix.eternalnature.testing;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.entity.shadow.ArmorStandBodyPart;
import me.masstrix.eternalnature.core.entity.shadow.ItemSlot;
import me.masstrix.eternalnature.core.entity.shadow.ShadowArmorStand;
import me.masstrix.eternalnature.core.render.LeafParticle;
import me.masstrix.eternalnature.util.SimplexNoiseOctave;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class TestCommand {

    private EternalNature plugin;

    public TestCommand(EternalNature plugin) {
        this.plugin = plugin;
    }

    ShadowArmorStand stand;

    public void execute(Player player, String[] args) {
        if (stand != null) {
            stand.remove();
        }

        Location loc = new Location(player.getWorld(), 362.5, 76, -455.5);

//        stand = new ShadowArmorStand(loc);
//        stand.setArms(true);
//        stand.setSlot(ItemSlot.HEAD, new ItemStack(Material.MAGMA_BLOCK));
//        stand.setSlot(ItemSlot.MAINHAND, new ItemStack(Material.BAMBOO));
//        stand.setSlot(ItemSlot.CHEST, new ItemStack(Material.IRON_CHESTPLATE));
//        stand.sendTo(player);
//        stand.setSlot(ItemSlot.LEGS, new ItemStack(Material.DIAMOND_LEGGINGS));
//
//        new BukkitRunnable() {
//            int i = 0;
//            SimplexNoiseOctave octave = new SimplexNoiseOctave(10);
//            double x = 0;
//            @Override
//            public void run() {
//                if (i++ == 20) {
//                    new BukkitRunnable() {
//                        @Override
//                        public void run() {
//                            stand.remove();
//                            stand = null;
//                        }
//                    }.runTaskLater(plugin, 20);
//                    this.cancel();
//                    return;
//                }
//
//                stand.setPose(ArmorStandBodyPart.BODY, new Vector(
//                        octave.noise(x), 0, 0));
//
//                stand.setPose(ArmorStandBodyPart.HEAD, new Vector(
//                        octave.noise(x + 10), 0, 0));
//
//                stand.setPose(ArmorStandBodyPart.LEFT_ARM, new Vector(
//                        octave.noise(x) * 2,
//                        0,
//                        octave.noise(x + 10) * 2));
//
//                stand.move(0, 0.1, 0);
//                x += 0.1;
//            }
//        }.runTaskTimer(plugin, 0, 1);
    }
}
