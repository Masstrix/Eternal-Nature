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
import me.masstrix.eternalnature.core.temperature.TemperatureIcon;
import me.masstrix.eternalnature.menus.Button;
import me.masstrix.eternalnature.menus.GlobalMenu;
import me.masstrix.eternalnature.menus.MenuManager;
import me.masstrix.eternalnature.menus.Menus;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.Lang;
import me.masstrix.lang.langEngine.LanguageEngine;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class LangSettingsMenu extends GlobalMenu {

    private EternalNature plugin;
    private MenuManager menuManager;
    private LanguageEngine le;

    public LangSettingsMenu(EternalNature plugin, MenuManager menuManager) {
        super(Menus.LANG_SETTINGS, 5);
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.le = plugin.getLanguageEngine();
    }

    @Override
    public String getTitle() {
        return le.getText("menu.language.title");
    }

    @Override
    public void build() {
        // Back button
        addBackButton(menuManager, Menus.SETTINGS);

        setButton(new Button(asSlot(1, 3), new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("&a" + le.getText("menu.language.reset.title"))
                .addDescription(le.getText("menu.language.reset.description"))
                .addLore("&e" + le.getText("menu.common.reset"))
                .build())
                .onClick(player -> {
                    plugin.writeLangFiles(true);
                    le.loadLanguages();
                    TemperatureIcon.reloadLang(le);
                    menuManager.forceCloseAll();
                    menuManager.rebuildAllMenus();
                    player.playSound(player.getLocation(), Sound.UI_LOOM_SELECT_PATTERN, 1, 1);
                    player.sendMessage(StringUtil.color(PluginData.PREFIX
                            + "Reset packaged language files."));
                }));

        setButton(new Button(asSlot(1, 5), new ItemBuilder(Material.BOOKSHELF)
                .setName("&a" + le.getText("menu.language.reload.title"))
                .addDescription(le.getText("menu.language.reload.description"))
                .addLore("&e" + le.getText("menu.common.reload"))
                .build())
                .onClick(player -> {
                    menuManager.forceCloseAll();
                    int count = le.loadLanguages();
                    TemperatureIcon.reloadLang(le);
                    menuManager.forceCloseAll();
                    menuManager.rebuildAllMenus();
                    player.playSound(player.getLocation(), Sound.UI_LOOM_SELECT_PATTERN, 1, 1);
                    player.sendMessage(StringUtil.color(PluginData.PREFIX
                            + "Reloaded language &7" + count + "&f languages."));
                }));

        // Load all languages into menu.
        int slot = 0;
        for (Lang lang : le.list()) {

            int row = slot / 7;
            int column = slot % 7;

            boolean selected = le.isActive(lang);

            // Add a loaded language to the menu.
            setButton(new Button(asSlot(row + 2, column + 1), () -> {
                ItemBuilder builder = new ItemBuilder(selected ? Material.MAP : Material.PAPER);
                builder.setGlowing(selected);
                builder.setName("&a" + lang.getNiceName());
                builder.addLore("");
                if (le.isActive(lang))
                    builder.addLore("&a" + le.getText("menu.common.selected"));
                else
                    builder.addLore("&e" + le.getText("menu.common.select"));
                return builder.build();
            }).onClick(player -> {
                if (selected) return;

                // Update config
                Configuration config = plugin.getRootConfig();;
                config.set(ConfigPath.LANGUAGE, lang.getLocale());
                config.save();

                // Apply language
                le.setLanguage(lang);
                menuManager.forceCloseAll();
                menuManager.rebuildAllMenus();
                player.playSound(player.getLocation(), Sound.UI_LOOM_SELECT_PATTERN, 1, 1);
                player.sendMessage(StringUtil.color(PluginData.PREFIX
                        + "Set language to &7" + lang.getNiceName()));

                // Reload temp icon names
                TemperatureIcon.reloadLang(le);
            }));

            slot++;
        }
    }
}
