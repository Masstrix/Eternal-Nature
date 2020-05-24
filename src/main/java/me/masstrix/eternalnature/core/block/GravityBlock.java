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

package me.masstrix.eternalnature.core.block;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.Serializable;

public abstract class GravityBlock implements Serializable, AbstractBlock {

    Location loc;
    Vector velocity;
    boolean onGround;
    boolean gravity;
    
    public GravityBlock(Location loc, Vector velocity) {
        this.loc = loc;
        this.velocity = velocity;
        BlockCache.add(this);
    }

    public GravityBlock(String data) {

    }

    public void update() {
        if (gravity) {

        }
    }

    @Override
    public Location getLocation() {
        return loc;
    }

    public String serialize() {
        return String.valueOf(loc.getX()) +
                loc.getY() +
                loc.getZ() +
                velocity.serialize();
    }
}
