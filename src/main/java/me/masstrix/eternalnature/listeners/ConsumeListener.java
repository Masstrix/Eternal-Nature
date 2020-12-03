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
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.player.UserData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.HashMap;
import java.util.Map;

@Configurable.Path("hydration.consumables")
public class ConsumeListener implements Listener, Configurable {

    private final EternalNature PLUGIN;
    private final Map<String, Double> AMOUNTS = new HashMap<>();

    public ConsumeListener(EternalNature plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        for (String s : section.getKeys(false)) {
            AMOUNTS.put(s.toLowerCase(), section.getDouble(s));
        }
    }

    /**
     * @param key name of material.
     * @return how much hydration the item gives when consumed.
     */
    private double getVal(String key) {
        return AMOUNTS.getOrDefault(key.toLowerCase(), 0D);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItem();
        Material type = stack.getType();

        UserData user = PLUGIN.getEngine().getUserData(player.getUniqueId());
        if (user == null) return;

        if (type == Material.POTION) {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            if (meta != null) {
                PotionType potionType = meta.getBasePotionData().getType();
                double val = getVal("potion");
                if (potionType == PotionType.WATER) {
                    val = getVal("water_bottle");
                }
                user.hydrate((float) val);
            }
        }
        double val = getVal(type.name());
        if (val != 0) {
            user.hydrate((float) val);
        }
    }
}
