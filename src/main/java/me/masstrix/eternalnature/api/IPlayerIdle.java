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

package me.masstrix.eternalnature.api;

public interface IPlayerIdle {

    /**
     * If the player is in an idle state they will have not done anything
     * for over 10 seconds.
     *
     * @return if the player is currently in an idle state.
     */
    boolean isIdle();

    /**
     * If a player is in a deep idle they will have not done anything for
     * over 2 minutes.
     *
     * @return if the player is currently in a deep idle.
     */
    boolean isDeepIdle();

    /**
     * If a player is marked as AFK there will have been no actions
     * from this player more than 10 minutes.
     *
     * @return if the player is afk.
     */
    boolean isAfk();
}
