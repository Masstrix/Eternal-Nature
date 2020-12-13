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

package me.masstrix.eternalnature.core.item;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.util.BooleanCompare;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Enum of all custom items added by the plugin.
 */
public enum CustomItem {

    LEAF("Leaf", Material.KELP, 88001);

    private NamespacedKey namespacedKey;
    private int customModel = -1;
    private Material type;
    private final ItemStack ITEM;
    private BooleanCompare<ItemStack> compare = i -> {
        ItemMeta meta = i.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (i.getType() != type) return false;
        if (meta.getCustomModelData() != customModel) return false;
        return container.has(namespacedKey, PersistentDataType.BYTE);
    };

    CustomItem(ItemStack stack) {
        ITEM = stack;
        defineItem();
    }

    CustomItem(String name, Material type) {
        this(name, type, -1);
    }

    CustomItem(String name, Material type, int model) {
        this(name, type, model, null);
    }

    CustomItem(String name, Material type, int model, BooleanCompare<ItemStack> compare) {
        ITEM = new ItemBuilder(type).setName("&f" + name).setCustomModelData(model).build();
        customModel = model;
        if (compare != null)
            this.compare = compare;
        defineItem();
    }

    public int getCustomModel() {
        return customModel;
    }

    public ItemStack get() {
        return ITEM;
    }

    /**
     * Checks and returns if the given item stack is the same type as the
     * custom item.
     *
     * @param stack item stack to check if it is the same.
     * @return if the item stack is this custom item.
     */
    public boolean isSameType(ItemStack stack) {
        return compare.isEqual(stack);
    }

    /**
     * Adds a custom tag to the item so it can be recognized as a custom item by
     * the plugin in the future.
     */
    private void defineItem() {
        this.type = ITEM.getType();
        JavaPlugin plugin = EternalNature.getPlugin(EternalNature.class);
        ItemMeta meta = ITEM.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "nature.customItem");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        ITEM.setItemMeta(meta);

        namespacedKey = key;
    }
}
