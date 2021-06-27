package me.masstrix.eternalnature.menus;

import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.core.item.ItemBuilder;
import me.masstrix.lang.langEngine.LanguageEngine;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SimpleButton extends Button{

    private LanguageEngine lang;

    public SimpleButton(int slot, Material material) {
        super(slot, new ItemStack(material));
    }

    public SimpleButton setLangEngine(LanguageEngine engine) {
        lang = engine;
        return this;
    }

    public SimpleButton context(String context) {
        title(context + ".title");
        description(context + ".description");
        return this;
    }

    public SimpleButton title(String index) {
        ItemBuilder i = new ItemBuilder(stack);
        i.setName(lang.getText(index), PluginData.Colors.PRIMARY);
        stack = i.build();
        return this;
    }

    public SimpleButton description(String index) {
        ItemBuilder i = new ItemBuilder(stack);
        i.addDescription(lang.getText(index), PluginData.Colors.MESSAGE);
        stack = i.build();
        return this;
    }

    public SimpleButton addSwitchView(String prefix, boolean toggle) {
        ItemBuilder i = new ItemBuilder(stack);
        i.addSwitchView(PluginData.Colors.MESSAGE + prefix, toggle);
        stack = i.build();
        return this;
    }

    public SimpleButton extra(String index, ChatColor color) {
        ItemBuilder i = new ItemBuilder(stack);
        i.addLore(color + lang.getText(index));
        stack = i.build();
        return this;
    }

    public enum Common {
        EDIT("edit"),
        CHANGE("change"),
        SELECTED("selected"),
        SELECT("select"),
        ENABLE("enable"),
        DISABLE("disable"),
        RESET("reset"),
        RELOAD("reload");

        String index;
        ChatColor color;

        Common(String index) {
            this.index = "menu.common." + index;
            this.color = PluginData.Colors.ACTION;
        }
    }

    public SimpleButton common(Common c) {
        return extra(c.index, c.color);
    }
}
