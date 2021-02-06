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

import me.masstrix.eternalnature.player.UserData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public interface TriggerCondition {

    /**
     * Creates a new condition from the configuration section.
     *
     * @param name    name of condition to get and return a new instance of.
     * @param section section to load the conditions data from.
     * @return a new instance of the queried condition or null if no condition with
     *         the same name exists.
     */
    static TriggerCondition createNewCondition(String name, ConfigurationSection section) {
        if ("hydration".equalsIgnoreCase(name)) {
            HydrationCondition c = new HydrationCondition();
            c.parse(section.getConfigurationSection("hydration"));
            return c;
        } else if ("temperature".equalsIgnoreCase(name)) {
            TemperatureCondition c = new TemperatureCondition();
            c.parse(section.getConfigurationSection("temperature"));
            return c;
        }
        return null;
    }

    /**
     * Parses the the condition from the conditions configuration section in the trigger.
     *
     * @param section an instance of the conditions configuration section.
     * @return an instance of this condition.
     */
    TriggerCondition parse(ConfigurationSection section);

    /**
     * @return the name of this condition.
     */
    String getName();

    /**
     * Returns if the condition is met. If true then the player has met this condition.
     *
     * @param user   data related to the player.
     * @param player player this condition is checking.
     * @return true if the player meets the requirements for this condition.
     */
    boolean isMet(UserData user, Player player);
}
