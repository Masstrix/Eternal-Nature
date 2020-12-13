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

package me.masstrix.eternalnature.api;

import org.bukkit.util.Vector;

public interface EternalUser {

    /**
     * Returns the motion vector of the player. This can be used
     * instead of {@code Player#getVecloty()} as it will return the
     * players true motion respecting any effects the player has and
     * if they are sprinting.
     *
     * @return the players motion vector.
     */
    Vector getMotion();

    /**
     * Returns if the player is currently moving.
     *
     * @return if the player is currently in motion.
     */
    boolean isInMotion();

    /**
     * @return if the player is currently marked as idle.
     */
    boolean isIdle();

    /**
     * @return the players idle info.
     */
    IPlayerIdle getPlayerIdleInfo();

    /**
     * @return the players current temperature,
     */
    double getTemperature();

    /**
     * @return the players current hydration. Ranges between 0 and 20.
     */
    double getHydration();

    /**
     * Hydrates the player.
     *
     * @param amount how much to hydrate the player by.
     */
    void hydrate(float amount);

    /**
     * Returns if the player currently has thirst. If the player is thirsty there
     * hydration will decrease at a faster than normal rate.
     *
     * @return if the player is currently thirsty.
     */
    boolean isThirsty();

    /**
     * @return how many seconds until the thirst effect has worn off.
     */
    int getThirstTime();

    /**
     * Adds time to the thirst effect of the player. This will stack on top of any
     * current time.
     *
     * @param sec time in seconds to add thirst for the player.
     */
    void addThirst(int sec);
}
