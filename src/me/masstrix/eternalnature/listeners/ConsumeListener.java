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
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ConsumeListener implements Listener {

    private EternalNature plugin;

    public ConsumeListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItem();
        Material type = stack.getType();

        UserData user = plugin.getEngine().getUserData(player.getUniqueId());
        if (user == null) return;

        if (type == Material.POTION) {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            if (meta != null) {
                PotionType potionType = meta.getBasePotionData().getType();
                user.hydrate(5);
            }
        }
        else if (type == Material.MILK_BUCKET) {
            user.hydrate(7);
        }
    }
}
