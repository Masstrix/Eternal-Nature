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
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;

public class LeafParticleMenu extends GlobalMenu {

    private EternalNature plugin;
    private MenuManager menuManager;
    private SystemConfig config;
    private LanguageEngine le;

    public LeafParticleMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.LEAF_PARTICLE_SETTINGS, 5);
        this.config = plugin.getSystemConfig();
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.le = plugin.getLanguageEngine();
    }

    @Override
    public String getTitle() {
        return le.getText("menu.leaf-particles.title");
    }

    @Override
    public void build() {
        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(getInventory(), asSlot(1, 3), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&a" + le.getText("menu.leaf-particles.enabled.title"))
                .addDescription(le.getText("menu.leaf-particles.enabled.description"))
                .addSwitch("Currently:", config.isEnabled(ConfigOption.LEAF_EFFECT))
                .build()).setToggle(le.getText("menu.leaf-particles.enabled.title"), () -> config.isEnabled(ConfigOption.LEAF_EFFECT))
                .onClick(player -> {
                    config.toggle(ConfigOption.LEAF_EFFECT);
                    config.save();
                    plugin.getEngine().updateSettings();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        ChangeToggleUtil spawnChances = new ChangeToggleUtil();
        spawnChances.add("&c" + le.getText("menu.leaf-particles.spawn.extreme"), 0.05);
        spawnChances.add("&c" + le.getText("menu.leaf-particles.spawn.high"), 0.01);
        spawnChances.add("&e" + le.getText("menu.leaf-particles.spawn.medium"), 0.005);
        spawnChances.add("&a" + le.getText("menu.leaf-particles.spawn.low"), 0.001);
        spawnChances.selectClosest(config.getDouble(ConfigOption.LEAF_EFFECT_CHANCE));

        setButton(new Button(getInventory(), asSlot(1, 5), () -> new ItemBuilder(Material.ENDER_EYE)
                .setName("&a" + le.getText("menu.leaf-particles.spawn.title"))
                .addDescription(le.getText("menu.leaf-particles.spawn.description"))
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
