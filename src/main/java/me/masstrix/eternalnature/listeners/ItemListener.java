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

package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.world.AgingItemWorker;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ItemListener implements Listener {

    private EternalNature plugin;

    public ItemListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(ItemSpawnEvent event) {
        Item item = event.getEntity();
        boolean plant = plugin.getEngine().getAutoPlanter().attemptToAddItem(item);
        if (!plant) {
            AgingItemWorker worker = (AgingItemWorker) plugin.getEngine().getWorker(AgingItemWorker.class);
            worker.attemptToAddItem(item);
        }
    }
}
