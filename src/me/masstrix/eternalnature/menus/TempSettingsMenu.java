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
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.potion.PotionType;

public class TempSettingsMenu extends GlobalMenu {

    public TempSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.TEMP_SETTINGS, "Temperature Settings", 5);
        SystemConfig config = plugin.getSystemConfig();

        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(getInventory(), asSlot(1, 2), () -> new ItemBuilder(Material.REDSTONE_TORCH)
                .setName("&aTemperature Enabled")
                .addLore("", "Set if temperature is enabled", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_ENABLED))
                .build()).setToggle("Enabled", () -> config.isEnabled(ConfigOption.TEMPERATURE_ENABLED))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_ENABLED);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 3), () -> new ItemBuilder(Material.IRON_SWORD)
                .setName("&aDamage")
                .addLore("", "Set if players will be hurt if", "there temperature is to high or low.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_DAMAGE))
                .build()).setToggle("Cause Damage", () -> config.isEnabled(ConfigOption.TEMPERATURE_DAMAGE))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_DAMAGE);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 4), () -> new ItemBuilder(Material.POTION)
                .setName("&aSweating")
                .setPotionType(PotionType.WATER)
                .addLore("", "If enabled players will sweat and lose", "hydration faster in higher temperatures.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_SWEAT))
                .build()).setToggle("Sweat", () -> config.isEnabled(ConfigOption.TEMPERATURE_SWEAT))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_SWEAT);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 5), () -> new ItemBuilder(Material.FLINT_AND_STEEL)
                .setName("&aBurn Damage")
                .addLore("", "If enabled and a player gets too hot", "they will ignite.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_BURN))
                .build()).setToggle("Burn", () -> config.isEnabled(ConfigOption.TEMPERATURE_BURN))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_BURN);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 6), () -> new ItemBuilder(Material.ICE)
                .setName("&aFreeze Damage")
                .addLore("", "If a player gets to cold they", "will begin to get damaged.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.TEMPERATURE_FREEZE))
                .build()).setToggle("Freeze", () -> config.isEnabled(ConfigOption.TEMPERATURE_FREEZE))
                .onClick(player -> {
                    config.toggle(ConfigOption.TEMPERATURE_FREEZE);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
        setButton(new Button(getInventory(), asSlot(4, 4), () -> new ItemBuilder(Material.JUNGLE_SIGN)
                .setName("&aDisplay Mode")
                .addLore("", "Set how how temperature is displayed", "to players.", "")
                .addLore("Currently: &f" + config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).getSimple(),
                        config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).getDescription())
                .addLore("&eClick to switch to &7" + config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).opposite().getSimple())
                .build())
                .onClick(player -> {
                    config.set(ConfigOption.TEMPERATURE_BAR_STYLE, config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE).opposite().name());
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
    }
}
