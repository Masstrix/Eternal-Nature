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
import me.masstrix.eternalnature.config.ConfigPath;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.util.ChangeToggleUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

@Configurable.Path("global.falling-leaves")
public class LeafParticleMenu extends GlobalMenu {

    private final EternalNature PLUGIN;
    private final MenuManager MANAGER;
    private final LanguageEngine LANG;
    private final Configuration CONFIG;

    private boolean enabled;
    private double chance;

    public LeafParticleMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.LEAF_PARTICLE_SETTINGS, 5);
        this.PLUGIN = plugin;
        this.MANAGER = menuManager;
        this.LANG = plugin.getLanguageEngine();
        this.CONFIG = plugin.getRootConfig();
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        chance = section.getDouble("spawn-chance");
        build();
    }

    @Override
    public String getTitle() {
        return LANG.getText("menu.leaf-particles.title");
    }

    @Override
    public void build() {
        // Back button
        addBackButton(MANAGER, Menus.SETTINGS);

        setButton(new Button(getInventory(), asSlot(1, 3), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&a" + LANG.getText("menu.leaf-particles.enabled.title"))
                .addDescription(LANG.getText("menu.leaf-particles.enabled.description"))
                .addSwitch("Currently:", enabled)
                .build()).setToggle(LANG.getText("menu.leaf-particles.enabled.title"), () -> enabled)
                .onClick(player -> {
                    CONFIG.set(ConfigPath.LEAF_EFFECT_ENABLED, (enabled = !enabled));
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        ChangeToggleUtil spawnChances = new ChangeToggleUtil();
        spawnChances.add("&c" + LANG.getText("menu.leaf-particles.spawn.extreme"), 0.05);
        spawnChances.add("&c" + LANG.getText("menu.leaf-particles.spawn.high"), 0.01);
        spawnChances.add("&e" + LANG.getText("menu.leaf-particles.spawn.medium"), 0.005);
        spawnChances.add("&a" + LANG.getText("menu.leaf-particles.spawn.low"), 0.001);
        spawnChances.selectClosest(chance);

        setButton(new Button(getInventory(), asSlot(1, 5), () -> new ItemBuilder(Material.ENDER_EYE)
                .setName("&a" + LANG.getText("menu.leaf-particles.spawn.title"))
                .addDescription(LANG.getText("menu.leaf-particles.spawn.description"))
                .addLore("Currently: " +  spawnChances.getSelected().getName())
                .addLore("&eChange to " +  spawnChances.getNext().getName())
                .build())
                .onClick(player -> {
                    spawnChances.next();
                    double chance = spawnChances.getSelected().getChance();
                    CONFIG.set(ConfigPath.LEAF_EFFECT_MAX_PARTICLES, chance);
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
    }
}
