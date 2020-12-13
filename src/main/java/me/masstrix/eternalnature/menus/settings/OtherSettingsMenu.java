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

@Configurable.Path("general")
public class OtherSettingsMenu extends GlobalMenu {

    private EternalNature plugin;
    private MenuManager menuManager;
    private Configuration config;
    private LanguageEngine le;

    private boolean updateNotify;
    private boolean updateCheck;

    public OtherSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.OTHER_SETTINGS, 5);
        this.menuManager = menuManager;
        config = plugin.getRootConfig();
        this.le = plugin.getLanguageEngine();
        this.plugin = plugin;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        updateCheck = section.getBoolean("check-for-updates");
        updateNotify = section.getBoolean("notify-update-join");
        build();
    }

    @Override
    public String getTitle() {
        return le.getText("menu.other.title");
    }

    @Override
    public void build() {
        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(asSlot(1, 3), () -> new ItemBuilder(Material.PAPER)
                .setName("&a" + le.getText("menu.other.update-notify.title"))
                .addDescription(le.getText("menu.other.update-notify.description"))
                .addSwitch("Currently:", updateNotify)
                .build()).setToggle(le.getText("menu.other.update-notify.title"),
                () -> updateNotify)
                .onClick(player -> {
                    config.toggle(ConfigPath.UPDATE_NOTIFY);
                    config.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));

        setButton(new Button(asSlot(1, 4), () -> new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("&a" + le.getText("menu.other.update-checks.title"))
                .addDescription(le.getText("menu.other.update-checks.description"))
                .addSwitch("Currently:", updateCheck)
                .build()).setToggle(le.getText("menu.other.update-checks.title"),
                () -> updateCheck)
                .onClick(player -> {
                    config.toggle(ConfigPath.UPDATE_CHECK);
                    config.save().reload();
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }));
    }
}
