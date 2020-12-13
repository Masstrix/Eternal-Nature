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

package me.masstrix.eternalnature.player;

import me.masstrix.eternalnature.api.IPlayerIdle;
import org.bukkit.Location;

public class PlayerIdle implements IPlayerIdle {

    private int x, y, z;
    private boolean idle;
    private boolean deepIdle;
    private boolean afk;
    private long lastMovement;

    /**
     * Checks if the player has moved.
     *
     * @param loc current location of the player.
     */
    public void check(Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if (x != this.x || y != this.y || z != this.z) {
            lastMovement = System.currentTimeMillis();
            this.x = x;
            this.y = y;
            this.z = z;
            idle = false;
            deepIdle = false;
            afk = false;
        }
        else if (!idle)
            idle = System.currentTimeMillis() - lastMovement > 10000; // 10s
        else if (!deepIdle)
            deepIdle = System.currentTimeMillis() - lastMovement > 120000; // 2m
        else if (!afk)
            afk = System.currentTimeMillis() - lastMovement > 600000; // 10m
    }

    /**
     * @return if the player is in a light idle state. This means the player has been
     *         standing in the same location for a short period but may still be active.
     */
    public boolean isIdle() {
        return idle;
    }

    /**
     * @return if the player is in a deep idle state. The player may still be active but it is likely
     *         they might be afk.
     */
    public boolean isDeepIdle() {
        return deepIdle;
    }

    /**
     * @return if the player is likely afk. Once the player has not moved for an extended period
     *         of time they will be deemed as afk.
     */
    public boolean isAfk() {
        return afk;
    }
}
