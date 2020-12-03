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

package me.masstrix.eternalnature.core.particle;

import me.masstrix.eternalnature.Ticking;
import me.masstrix.eternalnature.core.entity.EternalEntity;
import me.masstrix.eternalnature.core.world.wind.Wind;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Base for custom particles. This stores the core attributes that would be needed
 * for most particles and implements the base methods needed.
 * <p>
 * It's recommended to use a {@link me.masstrix.eternalnature.core.entity.shadow.ShadowEntity} when
 * creating particles so there are not issues with them becoming stuck in the world if using entities
 * to create the effect.
 */
public abstract class BaseParticle implements Ticking, EternalEntity {

    Wind wind;
    boolean alive;
    int lifeTime;
    Vector velocity = new Vector();

    public BaseParticle setForces(Wind wind) {
        this.wind = wind;
        return this;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    /**
     * @return the particles current velocity.
     */
    public Vector getVelocity() {
        return velocity;
    }

    /**
     * Pushes the particle in a direction.
     *
     * @param x x velocity to add.
     * @param y y velocity to add.
     * @param z z velocity to add.
     */
    public void push(double x, double y, double z) {
        push(new Vector(x, y, z));
    }

    /**
     * Pushes the particle in a direction.
     *
     * @param velocity velocity to add to the particles current velocity.
     */
    public void push(Vector velocity) {
        this.velocity.add(velocity);
    }

    @Override
    public abstract void tick();

    @Override
    public void remove() {
        alive = false;
    }

    /**
     * @return the current location of the particle.
     */
    public abstract Location getLocation();

    /**
     * Converts an angle in degrees to euler.
     *
     * @param v angle in degrees.
     * @return the angle as a euler unit.
     */
    final double degreesToEuler(double v) {
        return (v / 180) * Math.PI;
    }
}
