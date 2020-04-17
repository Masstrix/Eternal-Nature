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

package me.masstrix.eternalnature.menus;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.util.ChangeToggleUtil;
import org.bukkit.Material;
import org.bukkit.Sound;

public class LeafParticleMenu extends GlobalMenu {

    public LeafParticleMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.LEAF_PARTICLE_SETTINGS, "Leaf Particle Settings", 5);
        SystemConfig config = plugin.getSystemConfig();

        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(getInventory(), asSlot(1, 3), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&aLeaf Particles Enabled")
                .addLore("", "Set if lef particles are enabled.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.LEAF_EFFECT))
                .build()).setToggle("Leaf Particles", () -> config.isEnabled(ConfigOption.LEAF_EFFECT))
                .onClick(player -> {
                    config.toggle(ConfigOption.LEAF_EFFECT);
                    config.save();
                    plugin.getEngine().updateSettings();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        ChangeToggleUtil spawnChances = new ChangeToggleUtil();
        spawnChances.add("&cVERY HIGH", 0.05);
        spawnChances.add("&cHIGH", 0.01);
        spawnChances.add("&eMEDIUM", 0.005);
        spawnChances.add("&aLOW", 0.001);
        spawnChances.selectClosest(config.getDouble(ConfigOption.LEAF_EFFECT_CHANCE));

        setButton(new Button(getInventory(), asSlot(1, 4), () -> new ItemBuilder(Material.ENDER_EYE)
                .setName("&aSpawn Chance")
                .addLore("", "Set the spawn rate of particles.", "")
                .addLore("Currently: " +  spawnChances.getSelected().getName())
                .addLore("&eChange to " +  spawnChances.getNext().getName())
                .build())
                .onClick(player -> {
                    spawnChances.next();
                    config.set(ConfigOption.LEAF_EFFECT_CHANCE, spawnChances.getSelected().getChance());
                    config.save();
                    plugin.getEngine().updateSettings();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
    }
}
