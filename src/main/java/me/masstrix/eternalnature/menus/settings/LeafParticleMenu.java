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

package me.masstrix.eternalnature.menus.settings;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.config.ConfigPath;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.core.world.LeafEmitter;
import me.masstrix.eternalnature.menus.Button;
import me.masstrix.eternalnature.menus.GlobalMenu;
import me.masstrix.eternalnature.menus.MenuManager;
import me.masstrix.eternalnature.menus.Menus;
import me.masstrix.eternalnature.util.ChangeToggleUtil;
import me.masstrix.eternalnature.util.EnumUtils;
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

    private boolean alwaysReachGround;
    private boolean enabled;
    private double chance;
    private LeafEmitter.EmitterType emitterType;

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
        alwaysReachGround = section.getBoolean("always-reach-ground");
        emitterType = EnumUtils.findMatch(LeafEmitter.EmitterType.values(),
                section.getString("emitter-type"),
                LeafEmitter.EmitterType.ENTITY);
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

        setButton(new Button(asSlot(1, 3), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName(LANG.getText("menu.leaf-particles.enabled.title"))
                .addDescription(LANG.getText("menu.leaf-particles.enabled.description"))
                .addSwitch("Currently:", enabled)
                .build()).setToggle(LANG.getText("menu.leaf-particles.enabled.title"), () -> enabled)
                .onClick(player -> {
                    CONFIG.set(ConfigPath.LEAF_EFFECT_ENABLED, (enabled = !enabled));
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(1, 4), () -> new ItemBuilder(Material.GRASS_BLOCK)
                .setName(LANG.getText("menu.leaf-particles.reach-ground.title"))
                .addDescription(LANG.getText("menu.leaf-particles.reach-ground.description"))
                .addSwitch("Currently:", alwaysReachGround)
                .build()).setToggle(LANG.getText("menu.leaf-particles.reach-ground.title"), () -> alwaysReachGround)
                .onClick(player -> {
                    CONFIG.set(ConfigPath.LEAF_EFFECT_ALWAYS_REACH_GROUND, (alwaysReachGround = !alwaysReachGround));
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        ChangeToggleUtil spawnChances = new ChangeToggleUtil();
        spawnChances.add("&c" + LANG.getText("menu.leaf-particles.spawn.extreme"), 0.05);
        spawnChances.add("&c" + LANG.getText("menu.leaf-particles.spawn.high"), 0.01);
        spawnChances.add("&e" + LANG.getText("menu.leaf-particles.spawn.medium"), 0.005);
        spawnChances.add("&a" + LANG.getText("menu.leaf-particles.spawn.low"), 0.001);
        spawnChances.selectClosest(chance);

        setButton(new Button(asSlot(1, 5), () -> new ItemBuilder(Material.ENDER_EYE)
                .setName(LANG.getText("menu.leaf-particles.spawn.title"))
                .addDescription(LANG.getText("menu.leaf-particles.spawn.description"))
                .addLore("Currently: " +  spawnChances.getSelected().getName())
                .addAction("Change to " +  spawnChances.getNext().getName())
                .build())
                .onClick(player -> {
                    spawnChances.next();
                    double chance = spawnChances.getSelected().getChance();
                    CONFIG.set(ConfigPath.LEAF_EFFECT_CHANCE, chance);
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));


        setButton(new Button(asSlot(4, 4), () -> new ItemBuilder(Material.JUNGLE_SIGN)
                .setName(LANG.getText("menu.leaf-particles.emitter-type.title"))
                .addDescription(LANG.getText("menu.leaf-particles.emitter-type.description"))
                .addLore("Currently: &f" + emitterType.getSimple())
                .addAction("Click to switch to " + PluginData.Colors.MESSAGE + emitterType.next().getSimple())
                .build())
                .onClick(player -> {
                    CONFIG.set("global.falling-leaves.emitter-type", emitterType.next().name());
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
    }
}
