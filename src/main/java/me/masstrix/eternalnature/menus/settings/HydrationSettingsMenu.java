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
import me.masstrix.eternalnature.config.*;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.menus.Button;
import me.masstrix.eternalnature.menus.GlobalMenu;
import me.masstrix.eternalnature.menus.MenuManager;
import me.masstrix.eternalnature.menus.Menus;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionType;

@Configurable.Path("hydration")
public class HydrationSettingsMenu extends GlobalMenu {

    private MenuManager menuManager;
    private LanguageEngine le;
    private final Configuration CONFIG;

    private boolean enabled;
    private boolean doDamage;
    private boolean doActivity;
    private boolean doThirst;
    private boolean doSweat;
    private boolean canDrinkOcean;
    private boolean displayEnabled;
    private StatusRenderMethod renderMethod;
    private double thirstMod;

    public HydrationSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.HYDRATION_SETTINGS, 5);
        this.CONFIG = plugin.getRootConfig();
        this.menuManager = menuManager;
        this.le = plugin.getLanguageEngine();
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        doDamage = section.getBoolean("damage.enabled");
        doActivity = section.getBoolean("increase-from-activity");
        doThirst = section.getBoolean("thirst-effect.enabled");
        thirstMod = section.getDouble("thirst-effect.amount");
        canDrinkOcean = section.getBoolean("drink-from-open-water");
        displayEnabled = section.getBoolean("display.enabled");
        renderMethod = StatusRenderMethod.valueOf(section.getString("display.style"));
        doSweat = section.getBoolean("sweat");
        build();
    }

    @Override
    public String getTitle() {
        return le.getText("menu.hydration.title");
    }

    @Override
    public void build() {
        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(asSlot(1, 2), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName(le.getText("menu.hydration.enabled.title"))
                .addDescription(le.getText("menu.hydration.enabled.description"))
                .addSwitch("Currently:", enabled)
                .build()).setToggle(le.getText("menu.hydration.enabled.title"),
                () -> enabled)
                .onClick(player -> {
                    CONFIG.toggle("hydration.enabled");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(1, 3), () -> new ItemBuilder(Material.IRON_SWORD)
                .setName(le.getText("menu.hydration.damage.title"))
                .addDescription(le.getText("menu.hydration.damage.description"))
                .addSwitch("Currently:", doDamage)
                .build()).setToggle(le.getText("menu.hydration.damage.title"),
                () -> doDamage)
                .onClick(player -> {
                    CONFIG.set("hydration.damage.enabled", !doDamage);
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(1, 4), () -> new ItemBuilder(Material.RABBIT_FOOT)
                .setName(le.getText("menu.hydration.activity.title"))
                .addDescription(le.getText("menu.hydration.activity.description"))
                .addSwitch("Currently:", doActivity)
                .build()).setToggle(le.getText("menu.hydration.activity.title"),
                () -> doActivity)
                .onClick(player -> {
                    CONFIG.toggle("hydration.increase-from-activity");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(1, 5), () -> new ItemBuilder(Material.LINGERING_POTION)
                .setName(le.getText("menu.hydration.thirst.title"))
                .addDescription(le.getText("menu.hydration.thirst.description")
                        .replace("%amount%", "" + thirstMod))
                .addSwitch("Currently:", doThirst)
                .build()).setToggle(le.getText("menu.hydration.thirst.title"),
                () -> doThirst)
                .onClick(player -> {
                    CONFIG.toggle("hydration.thirst-effect.enabled");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(1, 6), () -> new ItemBuilder(Material.WATER_BUCKET)
                .setName(le.getText("menu.hydration.open_water.title"))
                .addDescription(le.getText("menu.hydration.open_water.description"))
                .addSwitch("Currently:", canDrinkOcean)
                .build()).setToggle(le.getText("menu.hydration.open_water.title"),
                () -> canDrinkOcean)
                .onClick(player -> {
                    CONFIG.toggle("hydration.drink-from-open-water");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        // Temperature Sweating toggle
        setButton(new Button(asSlot(1, 7), () -> new ItemBuilder(Material.POTION)
                .setPotionType(PotionType.WATER)
                .setName(le.getText("menu.temp.sweat.title"))
                .addDescription(le.getText("menu.temp.sweat.description"))
                .addSwitch("Currently:", doSweat)
                .build()).setToggle(le.getText("menu.temp.sweat.title"),
                () -> doSweat)
                .onClick(player -> {
                    CONFIG.toggle("hydration.sweat");
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(4, 4), () -> new ItemBuilder(Material.JUNGLE_SIGN)
                .setName(le.getText("menu.hydration.display.title"))
                .addDescription(le.getText("menu.hydration.display.description"))
                .addLore("Currently: &f" + renderMethod.getSimple(),
                        renderMethod.getDescription())
                .addAction("Click to switch to " + PluginData.Colors.MESSAGE + renderMethod.next().getSimple())
                .build())
                .onClick(player -> {
                    CONFIG.set("hydration.display.style", renderMethod.next().name());
                    CONFIG.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(4, 5), () ->
                new ItemBuilder(displayEnabled ? Material.LIME_BANNER : Material.GRAY_BANNER)
                        .setName( le.getText("menu.display.enabled.title"))
                        .addDescription(le.getText("menu.display.enabled.description"))
                        .addSwitch("Currently:", displayEnabled)
                        .build()).onClick(player -> {
            CONFIG.toggle("hydration.display.enabled");
            CONFIG.save().reload();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        }));
    }
}
