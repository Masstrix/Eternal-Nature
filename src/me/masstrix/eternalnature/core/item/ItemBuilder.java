package me.masstrix.eternalnature.core.item;

import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.Material;
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

    public ItemBuilder addSwitch(String prefix, boolean toggle) {
        addLore(prefix + (toggle ? "&a Enabled" : "&c Disabled"),
                "&eClick to " + (toggle ? "disable" : "enable"));
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

    public ItemStack build() {
        ItemStack item = stack == null ? new ItemStack(type) : stack;

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS,
                ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        if (meta instanceof PotionMeta && potionType != null) {
            PotionMeta potionMeta = (PotionMeta) meta;
            potionMeta.setBasePotionData(new PotionData(potionType));
            item.setItemMeta(potionMeta);
        }
        return item;
    }
}
