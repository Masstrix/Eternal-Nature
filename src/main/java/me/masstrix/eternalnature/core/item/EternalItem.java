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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EternalItem {

    public static final int MODEL_BASE = 88000;

    ItemBuilder stack;

    EternalItem(Material material) {
        stack = new ItemBuilder(material);
    }

    EternalItem(ItemStack stack) {
        this.stack = new ItemBuilder(stack);
    }

    EternalItem(EternalItem item) {
        this.stack = new ItemBuilder(item.stack.build());
    }

    EternalItem setModel(int id) {
        stack.setCustomModelData(id);
        return this;
    }

    EternalItem setName(String name) {
        stack.setName(name);
        return this;
    }

    EternalItem addLore(String... lore) {
        stack.addLore(lore);
        return this;
    }

    public ItemStack create() {
        return stack.build();
    }
}
