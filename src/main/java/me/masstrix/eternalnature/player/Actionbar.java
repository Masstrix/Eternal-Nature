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
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.TreeSet;

public class Actionbar {

    private final Set<ActionbarItem> items;
    private final Player player;
    private TextComponent bar;
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
    public String getBar() {
        return bar.getText();
    }

    /**
     * Adds a new item to the actionbar.
     *
     * @param item item to add.
     */
    public void add(ActionbarItem item) {
        items.add(item);
    }

    /**
     * Removes an item from the actionbar.
     *
     * @param item item to remove.
     */
    public void remove(ActionbarItem item) {
        items.remove(item);
    }

    /**
     * Reconstructs the actionbar. This will loop through all the added items and construct
     * them into a string that is sent to the player.
     */
    public void reconstruct() {
        StringBuilder text = new StringBuilder();
        boolean first = true;
        for (ActionbarItem item : items) {
            if (!first) text.append(" ");
            text.append("&f");
            text.append(item.getActionbarText());
            text.append("&f");
            first = false;
        }
        bar = new TextComponent(text.toString());
        render = text.length() > 0;
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
