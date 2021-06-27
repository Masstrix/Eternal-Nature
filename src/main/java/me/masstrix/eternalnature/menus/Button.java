/*
 * Copyright 2019 Matthew Denton
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

import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Buttons are a more simple to use way of handling items with actions
 * in an inventory. Being able to store a click action along with an
 * update method it makes handling a menus icon a bit easier.
 */
public class Button {

    private GlobalMenu menu;
    private final UpdateIcon UPDATER;
    private final int SLOT;
    ItemStack stack;
    private UpdateToggle updateToggle;
    private ClickEvent clickEvent;
    private boolean toggleDisplay = false, enabled;
    private String toggleName = "";

    /**
     * Creates a new button.
     *
     * @param slot slot this button is to be displayed in.
     * @param icon icon used to display for this button.
     */
    public Button(int slot, ItemStack icon) {
        this(slot, () -> icon);
    }

    /**
     * Creates a new button.
     *
     * @param slot slot this button is to be displayed in.
     * @param updateIcon method used to get the buttons icon.
     */
    public Button(int slot, UpdateIcon updateIcon) {
        this.SLOT = slot;
        this.UPDATER = updateIcon;
        stack = updateIcon.run();
    }

    Button setMenu(GlobalMenu menu) {
        this.menu = menu;
        return this;
    }

    /**
     * Sets the click method.
     *
     * @param event method used to handle the click event.
     */
    public Button onClick(ClickEvent event) {
        clickEvent = event;
        return this;
    }

    public Button setToggle(String name, UpdateToggle toggle) {
        toggleDisplay = true;
        updateToggle = toggle;
        this.toggleName = StringUtil.color(name);
        return this;
    }

    /**
     * Attempt to click this button. The clicked inventory and slot
     * must be the same as what was specified when creating the button
     * for the click to be actioned.
     *
     * @param player who clicked this button.
     * @param menu   inventory that was clicked in.
     * @param slot   slot that was clicked.
     */
    final void click(Player player, GlobalMenu menu, int slot) {
        if (this.menu == menu && slot == this.SLOT && clickEvent != null) {
            clickEvent.click(player);
            update();
        }
    }

    /**
     * Updates the icon for the button by running its update method.
     */
    public void update() {
        if (menu == null) return;
        stack = UPDATER.run();
        Inventory inv = menu.getInventory();
        if (inv == null) {
            menu.rebuild();
            inv = menu.getInventory();
            if (inv == null) return; // Critical error
        }
        inv.setItem(SLOT, stack);
        if (toggleDisplay) {
            if (updateToggle != null)
                enabled = updateToggle.toggle();
            inv.setItem(SLOT + 9, new ItemBuilder(enabled ? Material.LIME_STAINED_GLASS_PANE
                    : Material.GRAY_STAINED_GLASS_PANE)
                    .setName((enabled ? "&a" : "&8") + toggleName).build());
        }
    }

    /**
     * @return the slot the button is set in the inventory.
     */
    public final int getSlot() {
        return SLOT;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Button)) return false;
        Button button = (Button) o;
        return SLOT == button.SLOT;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(SLOT);
    }

    /**
     * Used for creating an updating icon for a button.
     */
    public interface UpdateIcon {
        ItemStack run();
    }

    /**
     * Used for setting the toggle state of a button.
     */
    public interface UpdateToggle {
        boolean toggle();
    }

    /**
     * Called by the button when it is clicked on.
     */
    public interface ClickEvent {
        void click(Player player);
    }
}
