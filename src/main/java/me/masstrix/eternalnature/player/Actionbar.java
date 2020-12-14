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

package me.masstrix.eternalnature.player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.TreeSet;

public class Actionbar {

    private final Set<ActionbarItem> items;
    private final Player player;
    private BaseComponent[] bar;
    private String text;
    private TextComponent spacer = new TextComponent(" ");
    private boolean render = false;
    private boolean prepared = false;

    public Actionbar(Player player) {
        this.player = player;
        items = new TreeSet<>((incoming, checked) -> {
            if (incoming == checked) return 0;
            ActionbarItem.Before before = incoming.getClass().getAnnotation(ActionbarItem.Before.class);
            return before != null && before.value().equals(incoming.getName()) ? -1 : 1;
        });
    }

    /**
     * @return the actionbar.
     */
    public String getText() {
        return text;
    }

    /**
     * @return the spacer used between items.
     */
    public TextComponent getSpacer() {
        return spacer;
    }

    /**
     * Sets the spacer used between each item in the actionbar. By default this
     * is a single space.
     *
     * @param spacer component to use as a spacer.
     */
    public void setSpacer(TextComponent spacer) {
        this.spacer = spacer == null ? new TextComponent(" ") : spacer;
    }

    /**
     * Adds a new item to the actionbar.
     *
     * @param item item to add.
     */
    public void add(ActionbarItem item) {
        items.add(item);
        prepare();
    }

    /**
     * Removes an item from the actionbar.
     *
     * @param item item to remove.
     */
    public void remove(ActionbarItem item) {
        items.remove(item);
        prepare();
    }

    /**
     * Reconstructs the actionbar. This will loop through all the added items and construct
     * them into a string that is sent to the player.
     */
    public void reconstruct() {
        StringBuilder legacy = new StringBuilder();
        ComponentBuilder builder = new ComponentBuilder();
        boolean first = true;
        render = false;
        for (ActionbarItem item : items) {
            BaseComponent[] append = item.getActionbarText();
            if (append == null || append.length == 0) continue;
            if (!first)
                builder.append(spacer, ComponentBuilder.FormatRetention.NONE);
            builder.append("", ComponentBuilder.FormatRetention.NONE);
            builder.append(append);
            first = false;
            render = true;
            for (BaseComponent b : append)
                legacy.append(b.toLegacyText());
        }
        text = legacy.toString();
        bar = builder.create();
    }

    /**
     * Sets the actionbar into a prepared state for the next time it is rendered. When the
     * actionbar is in this state it will reconstruct all the items the next time
     * {@link #send()} is called.
     */
    public void prepare() {
        prepared = true;
    }

    /**
     * Returns if the action bar is set to reconstruct on the next time {@link #render}
     * is called.
     *
     * @return if the action bar is currently prepared to reconstruct.
     */
    public boolean isPrepared() {
        return prepared;
    }

    /**
     * Sends the action bar to the player.
     */
    public void send() {
        if (prepared) {
            prepared = false;
            reconstruct();
        }
        if (!render) return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, bar);
    }
}
