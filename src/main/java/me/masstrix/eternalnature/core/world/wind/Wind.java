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

import java.util.Collection;
import java.util.List;

@Configurable.Path("global.wind")
public class Wind implements Ticking, Configurable {

    private final EternalNature PLUGIN;
    private final WorldData world;
    private final SimplexOctaveGenerator[] NOISE = new SimplexOctaveGenerator[2];
    private float fre = 0.03F;
    private float amp = 0.01F;
    private double gustStrength;
    private double ticks;

    //
    // Configuration
    //
    private boolean enabled;
    private boolean gustsEnabled;
    private double gustsChance;
    private double gustsMaxStrength;
    private boolean pushPlayers;
    private boolean pushEntities;

    public Wind(WorldData world, EternalNature plugin, long seed) {
        this.world = world;
        this.PLUGIN = plugin;
        ticks = MathUtil.randomInt(1000000);
        NOISE[0] = new SimplexOctaveGenerator(seed, 1);
        NOISE[0].setScale(0.01);
        NOISE[0].setWScale(2);

        NOISE[1] = new SimplexOctaveGenerator(seed + 6543, 1);
        NOISE[1].setScale(0.01);
        NOISE[1].setWScale(2);
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled", true);
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
     * @see #getForce(double, double, double) for more information.
     *
     * @param x x position.
     * @param y y position.
     * @param z z position.
     * @return the force of the wind in that location.
     */
    public Vector getForce(int x, int y, int z) {
        return this.getForce((double) x, y, z);
    }

    /**
     * Calculates and returns the wind force in the given location. This will return the
     * force as a directional vector with the length being how strong the wind is.
     *
     * @param x x position.
     * @param y y position.
     * @param z z position.
     * @return the force of the wind in that location.
     */
    public Vector getForce(double x, double y, double z) {
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
        double strength = NOISE[0].noise((x / 10) + (ticks / 10D), ticks, (z / 10) + (ticks / 10D),
                0.0001, 0.001);
        strength = Math.max(0, strength * 0.009);

        // Sample the area to get the average height around this
        // location.
        World world = this.world.asBukkit();
        if (world == null) return new Vector();
        final int SCAN_AREA = 16;
        final int SAMPLES = 10;
        int totalHeight = 0;
        for (int i = 0; i < SAMPLES; i++) {
            int sampleX = MathUtil.randomInt(-SCAN_AREA, SCAN_AREA);
            int sampleZ = MathUtil.randomInt(-SCAN_AREA, SCAN_AREA);
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

        if (fallOff == 0) return new Vector();

        return new Vector(cos, 0, sin)
                .rotateAroundZ(pseudoUpdraft)
                .normalize()
                .multiply((strength + gustStrength) * fallOff);
    }

    /**
     * Returns the current direction of the wind in the world for a specific location.
     * This will be a normalized vector for the current direction of the wind in that
     * location not accounting for world height.
     *
     * @param x x position.
     * @param z z position.
     * @return the wind direction for that position.
     */
    public Vector getWindDirection(double x, double z) {
        if (!enabled) return new Vector();
        final double SCALE = 0.1;
        double valX = NOISE[0].noise((x * SCALE), ticks, (z * SCALE), fre, amp);
        double valZ = NOISE[1].noise((x * SCALE), ticks, (z * SCALE), fre, amp);

        // Get the direction of the wind.
        double cos = Math.cos((valX * 2) - 1);
        double sin = Math.sin((valZ * 2) - 1);
        return new Vector(cos, 0, sin).normalize();
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
        //if (isGusty()) return;

        if (entity instanceof Player) {
            Player player = (Player) entity;
            GameMode gameMode = player.getGameMode();
            if (gameMode == GameMode.SPECTATOR || gameMode == GameMode.CREATIVE) {
                return;
            }
        }

        Location loc = entity.getLocation();
        Vector force = getForce(loc.getX(), loc.getY(), loc.getZ());

        Vector velocity = entity.getVelocity();
        entity.setVelocity(velocity.add(force));
    }

    private void runPhysics() {
        if (!enabled || !gustsEnabled || (!pushEntities && !pushPlayers)) return;
        if (!pushEntities) {
            Bukkit.getOnlinePlayers().forEach(this::push);
            return;
        }
        List<LivingEntity> entities = world.asBukkit().getLivingEntities();
        for (LivingEntity e : entities) {
            if (e instanceof Player && pushPlayers) push(e);
            else if (pushEntities) push(e);
        }
    }

    @Override
    public void tick() {
        ticks += 0.1;
        if (enabled && gustsEnabled) {
            double max = 0.01 * this.gustsMaxStrength;
            double fre = 0.06 * this.gustsChance;

            gustStrength = NOISE[0].noise(ticks, 1, 0.05) * 0.1;
            gustStrength = Math.max(0, gustStrength - fre);
            if (gustStrength > max)
                gustStrength = max;
        }
        runPhysics();
    }
}
