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
import me.masstrix.eternalnature.util.BooleanConsumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles a condition checking for basic math on a floating number. This allows for
 * the use of less-than, greater-than and equals checks.
 */
public abstract class MathCondition implements TriggerCondition {

    private final Set<BooleanConsumer<Float>> CHECKS = new HashSet<>();

    @Override
    public final MathCondition parse(ConfigurationSection section) {
        if (section.contains("less-than")) {
            CHECKS.add(value -> value < section.getDouble("less-than"));
        }
        if (section.contains("greater-than")) {
            CHECKS.add(value -> value < section.getDouble("greater-than"));
        }
        if (section.contains("equals")) {
            CHECKS.add(value -> value < section.getDouble("equals"));
        }
        return this;
    }

    @Override
    public String getName() {
        return "Math Condition";
    }

    @Override
    public boolean isMet(UserData user, Player player) {
        for (BooleanConsumer<Float> c : CHECKS) {
            if (!c.accept(getValue(user))) return false;
        }
        return true;
    }

    /**
     * Returns the value used for checking if the condition is met.
     *
     * @param data an instance of the players data that's being checked.
     * @return the value for the conditional check.
     */
    public abstract float getValue(UserData data);
}
