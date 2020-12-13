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

import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.util.Flicker;
import me.masstrix.eternalnature.util.Pair;
import me.masstrix.eternalnature.util.SecondsFormat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

/**
 * Stat Renderers are renderers for players to display stats on the
 * players screen in some way. These are build to be
 */
public interface StatRenderer extends ActionbarItem, Configurable {

    Flicker FLASH = new Flicker(300);
    SecondsFormat TIME_FORMAT = new SecondsFormat();
    /**
     * Default flash color used for the warning flashing. This is a basic
     * component to save the time for continually creating new components.
     */
    BaseComponent[] FLASH_COLOR = new ComponentBuilder("")
            .color(ChatColor.RED)
            .create();

    /**
     * Renders the stat. This will cause it to update the stats display if it
     * can or update the display and call for it to be updated in the next
     * tick for the player.
     */
    void render();

    /**
     * Resets all the stats display.
     */
    void reset();
}
