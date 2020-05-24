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

package me.masstrix.eternalnature.core.item;

import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private ItemStack stack;
    private Material type;
    private String name;
    private List<String> lore = new ArrayList<>();
    private PotionType potionType;
    private boolean glowing;

    public ItemBuilder(Material type) {
        this.type = type;
    }

    public ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public ItemBuilder(SkullIndex skullSkin) {
        this.stack = skullSkin.asItem();
    }

    public ItemBuilder setName(String name) {
        this.name = StringUtil.color("&f" + name);
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        for (String s : lore) {
            this.lore.add(StringUtil.color("&7" + s));
        }
        return this;
    }

    /**
     * Adds a description to the item. Descriptions are padded on the top and bottom and
     * will auto-wrap words when over a threshold length.
     *
     * @param desc description text to append to the items lore.
     * @return an instance of the item builder.
     */
    public ItemBuilder addDescription(String desc) {
        StringBuilder line = new StringBuilder();
        addLore("");
        boolean complete = false;
        String[] words = desc.split(" ");
        for (int i = 0; i < words.length; i++) {
            String s = words[i];
            // Subtract the color codes from lines length
            int trulLen = line.length() - StringUtil.getColorCodeCount(line.toString()) * 2;
            if (trulLen >= 25) {
                addLore(line.toString());
                line = new StringBuilder();
                if (i >= words.length - 1)
                    complete = true;
            } else {
                complete = false;
            }
            line.append(s);
            line.append(" ");
        }
        if (!complete)
            addLore(line.toString());
        addLore("");
        return this;
    }

    public ItemBuilder addSwitch(String prefix, boolean toggle) {
        addLore(prefix + (toggle ? "&a Enabled" : "&c Disabled"),
                "&eClick to " + (toggle ? "disable" : "enable"));
        return this;
    }

    public ItemBuilder addSwitchView(String prefix, boolean toggle) {
        addLore(prefix + (toggle ? "&a Enabled" : "&c Disabled"));
        return this;
    }

    public ItemBuilder addSwitch(String prefix, String on, String off, boolean toggle) {
        addLore(prefix + (toggle ? on : off), "&eClick to " + (toggle ? off : on));
        return this;
    }

    public ItemBuilder setPotionType(PotionType type) {
        this.potionType = type;
        return this;
    }

    public ItemBuilder setGlowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public ItemStack build() {
        ItemStack item = stack == null ? new ItemStack(type) : stack;

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
        if (glowing) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        item.setItemMeta(meta);
        if (meta instanceof PotionMeta && potionType != null) {
            PotionMeta potionMeta = (PotionMeta) meta;
            potionMeta.setBasePotionData(new PotionData(potionType));
            item.setItemMeta(potionMeta);
        }
        return item;
    }
}
