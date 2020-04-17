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

public class OtherSettingsMenu extends GlobalMenu {

    public OtherSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.OTHER_SETTINGS, "Other Settings", 5);
        SystemConfig config = plugin.getSystemConfig();

        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(getInventory(), asSlot(1, 3), () -> new ItemBuilder(Material.PAPER)
                .setName("&aUpdate Notifications")
                .addLore("", "Notify's admins and ops on join", "if there is a newer version", "available.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.UPDATES_NOTIFY))
                .build()).setToggle("Update Checks", () -> config.isEnabled(ConfigOption.UPDATES_NOTIFY))
                .onClick(player -> {
                    config.toggle(ConfigOption.UPDATES_NOTIFY);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(getInventory(), asSlot(1, 4), () -> new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("&aUpdate Checks")
                .addLore("", "Toggle if automatic checks for updates", "are done.", "")
                .addSwitch("Currently:", config.isEnabled(ConfigOption.UPDATES_CHECK))
                .build()).setToggle("Update Checks", () -> config.isEnabled(ConfigOption.UPDATES_CHECK))
                .onClick(player -> {
                    config.toggle(ConfigOption.UPDATES_CHECK);
                    config.save();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
    }
}
