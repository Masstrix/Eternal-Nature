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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.*;

public final class MenuManager implements Listener {

    private EternalNature plugin;
    private Map<String, GlobalMenu> menus = new HashMap<>();

    public MenuManager(EternalNature plugin) {
        this.plugin = plugin;
    }

    public void register(GlobalMenu... menu) {
        for (GlobalMenu m : menu) {
            menus.put(m.getID(), m);
        }
    }

    public Collection<GlobalMenu> getMenus() {
        return menus.values();
    }

    public void forceCloseAll() {
        for (GlobalMenu m : menus.values()) {
            m.forceClose();
        }
    }

    public void rebuildAllMenus() {
        for (GlobalMenu m : menus.values()) {
            m.rebuild();
        }
    }

    public void remove(String id) {
        menus.remove(id);
    }

    /**
     * Returns a menu from an id. If the menu has not been registered then
     * null will be returned.
     *
     * @param id id of menu to get.
     * @return the menu with that id or null if none exist.
     */
    public GlobalMenu getMenu(Menus id) {
        return menus.get(id.getId());
    }

    /**
     * Returns a menu from an id. If the menu has not been registered then
     * null will be returned.
     *
     * @param id id of menu to get.
     * @return the menu with that id or null if none exist.
     */
    public GlobalMenu getMenu(String id) {
        return menus.get(id);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (event.getSlotType() == InventoryType.SlotType.OUTSIDE) return;
        if (inv == null) return;

        // Find a menu that matches then run the on click event if there is a match found.
        for (GlobalMenu menu : menus.values()) {
            if (menu.isInventorySimilar(inv)) {
//                menu.processClick(event.getSlot(), (Player) event.getWhoClicked(), event.getClick());
                event.setCancelled(true);
                break;
            }
        }
    }
}
