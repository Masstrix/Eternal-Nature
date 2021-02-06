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

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.Ticking;
import me.masstrix.eternalnature.player.UserData;
import me.masstrix.eternalnature.util.Stopwatch;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Triggers are designed to be executed on a tick based system by the {@link TriggerManager}
 * to preform actions when a player meets all the required conditions.
 */
public class Trigger implements Ticking {

    private final String NAME;
    private final int INTERVAL;
    private final boolean REPEAT;
    private boolean valid;
    private boolean executing;
    private final Stopwatch STOPWATCH = new Stopwatch().start();
    private final Set<TriggerAction> ACTIONS = new HashSet<>();
    private final Set<TriggerCondition> CONDITIONS = new HashSet<>();
    private final Set<UserData> PLAYER_CACHE = Collections.newSetFromMap(new WeakHashMap<>());
    private final EternalNature PLUGIN;

    /**
     * Creates a new trigger instance. This works by loading the information from a configuration
     * section and parsing all the data from it. The name of the trigger is the same as the section
     * provided.
     *
     * @param plugin  an instance of the plugin.
     * @param section the configuration section for this trigger.
     */
    Trigger(EternalNature plugin, ConfigurationSection section) {
        this.PLUGIN = plugin;
        this.REPEAT = section.getBoolean("repeat", false);
        this.INTERVAL = section.getInt("interval", 1000);
        this.NAME = section.getName();
        plugin.getDebugLogger().info("Loaded Trigger \"" + NAME + "\".");

        // Load all the conditions for the trigger.
        ConfigurationSection conditionSection = section.getConfigurationSection("conditions");
        if (conditionSection == null) return;
        for (String key : conditionSection.getKeys(false)) {
            TriggerCondition c = TriggerCondition.createNewCondition(key, conditionSection);
            if (c != null) {
                CONDITIONS.add(c);
                valid = true;
            } else {
                plugin.getDebugLogger().warning("Trigger " + NAME + " has an invalid condition \"" + key + "\".");
            }
        }

        ConfigurationSection actionsSection = section.getConfigurationSection("actions");
        ACTIONS.addAll(TriggerAction.parse(actionsSection));
    }

    /**
     * @return if this trigger is a repeating task.
     */
    public boolean doesRepeat() {
        return REPEAT;
    }

    /**
     * Tick this trigger. THis will only execute if the interval time has passed.
     */
    @Override
    public void tick() {
        if (!STOPWATCH.hasPassed(INTERVAL)) {
            return;
        }
        STOPWATCH.start();
        executing = true;

        // Remove all players that no longer meet the conditions for this trigger.
        // This will also execute the trigger for all the players who do meet all
        // the conditions.
        PLAYER_CACHE.stream().iterator().forEachRemaining(data
                -> attempt(data, Bukkit.getPlayer(data.getUniqueId())));
        executing = false;
    }

    /**
     * Checks if the player meets all the conditions required to execute this trigger. If the
     * player does execute the trigger then it will be added to a cache. If this trigger repeats
     * they will continue to have it executed for them until they no longer meet the conditions
     * required. If not a repeating type then they will only execute it once and be kept in cache
     * until they not longer meet the conditions.
     *
     * @param data   the players user data.
     * @param player the player to attempt to execute this trigger.
     * @return if the attempt was successful.
     */
    public boolean attempt(UserData data, Player player) {
        if (data == null || player == null || !valid) {
            if (data != null) PLAYER_CACHE.remove(data);
            return false;
        }
        // Look and check if all conditions are met for that player.
        boolean allMet = true;
        for (TriggerCondition condition : CONDITIONS) {
            if (!condition.isMet(data, player)) {
                allMet = false;
                break;
            }
        }

        // Removes the player from the cache if the conditions don't meet the requirements
        // but has executed this trigger before.
        if (!allMet) {
            boolean removed = PLAYER_CACHE.remove(data);
            if (removed)
                PLUGIN.getDebugLogger().info(player.getName() + " was removed from trigger " + NAME);
            return false;
        }

        // Execute the task for the player
        if ((!REPEAT && !PLAYER_CACHE.contains(data)) || (REPEAT && executing)) {
            execute(data, player);
        }

        // Add the player to cache.
        boolean added = PLAYER_CACHE.add(data);
        if (added)
            PLUGIN.getDebugLogger().info(player.getName() + " executed trigger " + NAME);
        return true;
    }

    /**
     * Executes the trigger for the specified player.
     *
     * @param data   the players user data.
     * @param player player to execute this trigger for.
     */
    private void execute(UserData data, Player player) {
        if (data == null || player == null || !valid) return;
        ACTIONS.stream().iterator().forEachRemaining(a -> a.execute(data, player));
    }

    /**
     * Removes all players from cache.
     */
    public void clear() {
        PLAYER_CACHE.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Trigger)) return false;
        Trigger trigger = (Trigger) o;
        return Objects.equals(NAME, trigger.NAME);
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }
}
