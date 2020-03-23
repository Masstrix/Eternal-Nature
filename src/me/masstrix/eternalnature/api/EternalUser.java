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

public interface EternalUser {

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
}
