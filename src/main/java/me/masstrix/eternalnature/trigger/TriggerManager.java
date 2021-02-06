/*
 * Copyright 2021 Matthew Denton
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

package me.masstrix.eternalnature.trigger;

import io.netty.util.internal.ConcurrentSet;
import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.player.UserData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Set;

public class TriggerManager {

    private final EternalNature PLUGIN;
    private final Configuration CONFIG;
    private final Set<Trigger> TRIGGERS = new ConcurrentSet<>();

    public TriggerManager(EternalNature plugin) {
        this.PLUGIN = plugin;
        CONFIG = new Configuration(plugin, "triggers").create(true);
    }

    /**
     * Loads all the triggers from the triggers.yml config into this manager to be
     * used.
     */
    public void load() {
        if (TRIGGERS.size() > 0) {
            TRIGGERS.forEach(this::unsubscribe);
        }
        CONFIG.reload();
        YamlConfiguration all = CONFIG.getYml();
        PLUGIN.getDebugLogger().info("Loading triggers");

        for (String key : all.getKeys(false)) {
            if (!all.isConfigurationSection(key)) continue;
            ConfigurationSection sec = all.getConfigurationSection(key);
            if (sec == null) continue;
            Trigger trigger = new Trigger(PLUGIN, sec);
            TRIGGERS.add(trigger);
            if (trigger.doesRepeat()) {
                PLUGIN.getEngine().getHeartbeat().subscribe(trigger);
            }
        }
    }

    /**
     * Removes all triggers from cache.
     */
    public void clear() {
        TRIGGERS.forEach(this::unsubscribe);
    }

    /**
     * Unsubscribes the trigger from the plugins heartbeat and cache. Once called
     * the trigger will no longer be known to the plugin until {@link #load()} is
     * called again.
     *
     * @param trigger trigger to remove from memory.
     */
    public void unsubscribe(Trigger trigger) {
        trigger.clear();
        TRIGGERS.remove(trigger);
        PLUGIN.getEngine().getHeartbeat().unsubscribe(trigger);
    }

    /**
     * Attempts to call any trigger for a player.
     *
     * @param data   data related to the player.
     * @param player player to call any trigger for.
     */
    public void attemptToTrigger(UserData data, Player player) {
        TRIGGERS.forEach(trigger -> trigger.attempt(data, player));
    }
}
