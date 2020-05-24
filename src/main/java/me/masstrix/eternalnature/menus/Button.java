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

/**
 * Buttons are a more simple to use way of handling items with actions
 * in an inventory. Being able to store a click action along with an
 * update method it makes handling a menus icon a bit easier.
 */
public class Button {

    private Inventory inventory;
    private int slot;
    private ItemStack stack;
    private UpdateIcon updateIcon;
    private UpdateToggle updateToggle;
    private ClickEvent click;
    private boolean toggleDisplay = false, enabled;
    private String toggleName = "";

    /**
     * Creates a new button.
     *
     * @param inventory inventory this button is linked to.
     * @param slot slot this button is to be displayed in.
     * @param icon icon used to display for this button.
     */
    public Button(Inventory inventory, int slot, ItemStack icon) {
        this(inventory, slot, () -> icon);
    }

    /**
     * Creates a new button.
     *
     * @param inventory inventory this button is linked to.
     * @param slot slot this button is to be displayed in.
     * @param updateIcon method used to get the buttons icon.
     */
    public Button(Inventory inventory, int slot, UpdateIcon updateIcon) {
        this.inventory = inventory;
        this.slot = slot;
        this.updateIcon = updateIcon;
        stack = updateIcon.run();
    }

    /**
     * Sets the click method.
     *
     * @param event method used to handle the click event.
     */
    public Button onClick(ClickEvent event) {
        click = event;
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
     * @param inv inventory that was clicked in.
     * @param slot slot that was clicked.
     */
    public void click(Player player, Inventory inv, int slot) {
        if (wasClicked(inv, slot) && click != null) {
            click.click(player);
            update();
        }
    }

    /**
     * Updates the icon for the button by running its update method.
     */
    public void update() {
        stack = updateIcon.run();
        inventory.setItem(slot, stack);
        if (toggleDisplay) {
            if (updateToggle != null)
                enabled = updateToggle.toggle();
            inventory.setItem(slot + 9, new ItemBuilder(enabled ? Material.LIME_STAINED_GLASS_PANE
                    : Material.GRAY_STAINED_GLASS_PANE)
                    .setName((enabled ? "&a" : "&8") + toggleName).build());
        }
    }

    public int getSlot() {
        return slot;
    }

    public boolean wasClicked(Inventory inv, int slot) {
        return inv == this.inventory && slot == this.slot;
    }

    interface UpdateIcon {
        ItemStack run();
    }

    interface UpdateToggle {
        boolean toggle();
    }

    interface ClickEvent {
        void click(Player player);
    }
}
