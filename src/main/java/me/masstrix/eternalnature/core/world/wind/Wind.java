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

package me.masstrix.eternalnature.core.world.wind;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.Ticking;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.List;

// TODO cache wind data to be pooled
@Configurable.Path("global.wind")
public class Wind implements Ticking, Configurable {

    private final EternalNature PLUGIN;
    private final WorldData world;
    private final SimplexOctaveGenerator[] NOISE = new SimplexOctaveGenerator[2];
    private float fre = 10F;
    private float amp = 0.01F;
    private double gustStrength;
    private double ticks;
    public final double TICK_PROGRESS = 0.1;

    //
    // Configuration
    //
    private boolean enabled;
    private boolean gustsEnabled;
    private double gustsChance;
    private double gustsMaxStrength;
    private boolean pushPlayers;
    private boolean pushEntities;

    // Defines how strong the wing has to be blowing before
    // entities are pushed by it.
    private double pushThreshold = 0.01;

    public Wind(WorldData world, EternalNature plugin, long seed) {
        this.world = world;
        this.PLUGIN = plugin;
        ticks = MathUtil.randomInt(1000000);
        NOISE[0] = new SimplexOctaveGenerator(seed, 2);
        NOISE[0].setScale(0.01);
        NOISE[0].setWScale(1);

        NOISE[1] = new SimplexOctaveGenerator(seed + 6543, 2);
        NOISE[1].setScale(0.01);
        NOISE[1].setWScale(1);
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled", true);
        pushThreshold = section.getDouble("push-threshold", 0.01);
        gustsEnabled = section.getBoolean("gusts.enabled", true);
        gustsChance = section.getDouble("gusts.chance", 1.0);
        gustsMaxStrength = section.getDouble("gusts.max-strength", 1.0);
        pushPlayers = section.getBoolean("gusts.push-players", true);
        pushEntities = section.getBoolean("gusts.push-entities", true);
    }

    /**
     * Sets the frequency used for the noise when getting the force of the wind.
     * The lower this number the more spread out the noise will be creating a
     * smoother effect.
     *
     * @param fre frequency to use.
     * @return an instance of this wind object.
     */
    public Wind setFrequency(float fre) {
        this.fre = fre;
        return this;
    }

    /**
     * Sets the amplitude used for the noise when getting the force of the wind.
     *
     * @param amp amplitude to use for the noise.
     * @return an instance of this wind object.
     */
    public Wind setAmplitude(float amp) {
        this.amp = amp;
        return this;
    }

    /**
     * Gets the force of the wind in a given location.
     *
     * @see #getDirection(double, double) for more information.
     *
     * @param x x position.
     * @param z z position.
     * @return the force of the wind in that location.
     */
    public Vector getDirection(int x, int z) {
        return this.getDirection((double) x, z);
    }

    /**
     * Calculates and returns the wind force in the given location. This will return the
     * force as a directional vector with the length being how strong the wind is.
     *
     * @param x x position.
     * @param z z position.
     * @return the normalized direction of the wind in that location.
     */
    public Vector getDirection(double x, double z) {
        if (!enabled) return new Vector();
        final double SCALE = 0.1;
        double valX = NOISE[0].noise((x * SCALE), ticks, (z * SCALE), fre, amp);
        double valZ = NOISE[1].noise((x * SCALE), ticks, (z * SCALE), fre, amp);

        // Get the direction of the wind.
        double cos = Math.cos((valX * 2) - 1);
        double sin = Math.sin((valZ * 2) - 1);

        // Sets the strength of the wind.
        double temp = world.getBiomeEmission((int) x, 0, (int) z);
        double pseudoUpdraft = temp * 0.001;

        return new Vector(cos, 0, sin)
                .rotateAroundZ(pseudoUpdraft)
                .normalize();
    }

    public double getWindSpeed(double x, double y, double z) {
        return getWindSpeed(x, y, z, 0);
    }

    /**
     * Calculates and return the wind speed in a specific location of the world. The
     * wind speed will decrease to 0 when it's location is below the average terrain height.
     *
     * @param x x position.
     * @param y y position.
     * @param z z position.
     * @param timeOffset Time offset to get the wind speed of. THis is useful if you want to
     *                   know the wind speed in the future.
     * @return the speed of the wind in the location.
     */
    public double getWindSpeed(double x, double y, double z, double timeOffset) {
        double time = ticks + timeOffset;
        double strength = NOISE[0].noise((x / 10) + (time / 10D), time * 0.3, (z / 10) + (time / 10D),
                0.0001, 0.3);
        strength = Math.max(0, strength + 1);

        // Scan around the location to get the average ground height.
        World world = this.world.asBukkit();
        final int SCAN_AREA = 16;
        final int SAMPLES = 10;
        int totalHeight = 0;
        for (int i = 0; i < SAMPLES; i++) {
            int sampleX = (int) (MathUtil.randomInt(-SCAN_AREA, SCAN_AREA) + x);
            int sampleZ = (int) (MathUtil.randomInt(-SCAN_AREA, SCAN_AREA) + z);
            Block block = world.getHighestBlockAt(sampleX, sampleZ);
            totalHeight += block.getY();
        }
        int averageHeight = totalHeight / SAMPLES;

        // Get the current gust strength
        double gustStrength = getGustStrength();
        if (!gustsEnabled) gustStrength = 0;

        double fallOff = y / averageHeight;
        if (y < averageHeight) {
            double diff = averageHeight - y;
            fallOff = diff > 10 ? 0 : (10 - diff) / 10;
        }
        if (fallOff > 1)
            fallOff = 1 + Math.min(1, fallOff * 0.05);

        return (strength + gustStrength) * fallOff;
    }

    /**
     * Gets the true wind speed. The force returned from this vector is far too high
     * to be used to be applied to an entity and should be multiplied by something like
     * 0.01 for a more accurate representation of the wind speed.
     *
     * @param x x world position.
     * @param y y world position.
     * @param z z world position.
     * @return  the motion vector of the wind in that location. This vector is facing the
     *          direction of the wind and has its length set to the wind speed.
     */
    public Vector getTrueWindForce(double x, double y, double z) {
        return getDirection(x, z).multiply(getWindSpeed(x, y, z));
    }

    /**
     * Returns the current strength of gusts. Gusts are not location specific so
     * are the same anywhere in the world.
     *
     * @return the current gust strength.
     */
    public double getGustStrength() {
        return enabled && gustsEnabled ? gustStrength : 0;
    }

    /**
     * Returns if the wind is gusty, when gusty entities and players will start getting pushed
     * by the wind.
     *
     * @return if it is currently gusty.
     */
    public boolean isGusty() {
        return getGustStrength() >= 0.01;
    }

    /**
     * Pushes an entity with the current wind conditions. Entities can only be
     * pushed if the wind is gusty.
     *
     * @param entity entity to push by the wind.
     */
    public void push(Entity entity) {
        if (entity instanceof Player player) {
            GameMode gameMode = player.getGameMode();
            if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.CREATIVE) {
                return;
            }
        }

//        Location loc = entity.getLocation();
//        Vector force = getDirection(loc.getX(), loc.getY(), loc.getZ());
//        double strength = force.length();
//
//
//        Vector currentVelocity = entity.getVelocity();
//        currentVelocity.add(force);
//
//        entity.setVelocity(currentVelocity);
    }

    /**
     * Gets the entities motion and adds the wind velocity to it.
     *
     * @see #apply(Vector, double, double, double)
     *
     * @param entity entity to get the motion from.
     * @return the entities motion with the wind velocity added to it.
     */
    public Vector apply(Entity entity) {
        if (entity instanceof Player player) {
            GameMode gameMode = player.getGameMode();
            if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.CREATIVE) {
                return entity.getVelocity();
            }
        }

        Location loc = entity.getLocation();
        return apply(entity.getVelocity(), loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Applies the wind velocity to a motion vector.
     *
     * @param motion    motion vector to add.
     * @param x         x position.
     * @param y         y position.
     * @param z         z position.
     * @return the motion vector of the wind velocity and motion added together.
     */
    public Vector apply(Vector motion, double x, double y, double z) {
        Vector windDir = getDirection(x, z);
        double windSpeed = getWindSpeed(x, y, z);

        Vector wind = windDir.multiply(windSpeed * 0.001);
        return motion.add(wind);
    }

    // TODO re implement physics.
    private void runPhysics() {
        if (!enabled || (!pushEntities && !pushPlayers)) return;
        for (LivingEntity e : world.asBukkit().getLivingEntities()) {
            if (e instanceof Player && pushPlayers) push(e);
            //else if (pushEntities) push(e);
        }
    }

    @Override
    public void tick() {
        ticks += TICK_PROGRESS;
        if (enabled && gustsEnabled) {
            double max = 0.01 * this.gustsMaxStrength;
            double fre = 0.06 * this.gustsChance;

            gustStrength = NOISE[0].noise(ticks, 1, 0.05) * 0.1;
            gustStrength = Math.max(0, gustStrength - fre);
            if (gustStrength > max)
                gustStrength = max;
        }
        //runPhysics();
    }
}
