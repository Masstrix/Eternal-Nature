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

package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.core.particle.LeafParticle;
import me.masstrix.eternalnature.player.UserData;
import me.masstrix.eternalnature.util.BlockScanner;
import me.masstrix.eternalnature.util.EnumUtils;
import me.masstrix.eternalnature.util.IngestedTask;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Configurable.Path("global.falling-leaves")
public class LeafEmitter implements EternalWorker, Configurable {

    /**
     * Defines the emitter type for leaf particles. If using a custom resource pack
     * it would be more performant to use the particles otherwise entities will give
     * a more 3D effect and work more universally with additional control for wind.
     */
    public enum EmitterType {
        PARTICLE(1, "Particle") {
            @Override
            public EmitterType next() {
                return ENTITY;
            }
        },
        ENTITY(10, "Entity") {
            @Override
            public EmitterType next() {
                return PARTICLE;
            }
        };

        private final int TICK_RATE;
        private final String SIMPLE;

        EmitterType(int tickRate, String simple) {
            this.TICK_RATE = tickRate;
            this.SIMPLE = simple;
        }

        public String getSimple() {
            return SIMPLE;
        }

        /**
         * @return the next emitter type in the enum.
         */
        public EmitterType next() {
            return this;
        }
    }

    private boolean enabled;
    private double spawnChance = 0.005;
    private int maxParticles = 200;
    private int scanDelay = 2;
    private final EternalNature PLUGIN;
    private final Set<Location> LOCATIONS;
    private final Set<LeafParticle> PARTICLES;
    private final BlockScanner SCANNER;
    private BukkitTask updater, spawner;
    private LeafParticle.LeafOptions options = LeafParticle.LeafOptions.DEFAULT;
    private EmitterType emitterType = EmitterType.ENTITY;

    // Particle fields
    private Particle particle = Particle.FALLING_WATER;
    private int particleMin, particleMax;

    public LeafEmitter(EternalNature plugin) {
        this.PLUGIN = plugin;
        this.SCANNER = new BlockScanner(plugin);
        LOCATIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());
        PARTICLES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        enabled = section.getBoolean("enabled");
        scanDelay = section.getInt("scan-delay");
        maxParticles = section.getInt("max-particles");
        spawnChance = section.getDouble("spawn-chance");
        int range = section.getInt("range");
        int fidelity = section.getInt("fidelity");
        SCANNER.setScanScale(range, range + 4);
        SCANNER.setFidelity(fidelity);
        options = new LeafParticle.LeafOptions()
                .setLifeMax(section.getInt("life.max"))
                .setLifeMin(section.getInt("life.min"))
                .setForceReachGround(section.getBoolean("always-reach-ground"));

        // Set the particle emit type
        String type = section.getString("emitter-type");
        EmitterType typeBefore = this.emitterType;
        emitterType = EnumUtils.findMatch(EmitterType.values(), type, EmitterType.ENTITY);
        String part = section.getString("particle-settings.particle");
        particle = EnumUtils.findMatch(Particle.values(), part, Particle.ASH);
        particleMax = section.getInt("particle-settings.bunch-max", 5);
        particleMin = section.getInt("particle-settings.bunch-min", 1);

        // Make sure not leaf particles are left around if the emitter type is
        // changed while still running.
        if (typeBefore != emitterType) {
            PARTICLES.forEach(LeafParticle::remove);
            PARTICLES.clear();

            // Restart the spawner to make sure it's running at the correct tick
            // for that particle type.
            startSpawner();
        }
    }

    /**
     * Returns how many leaf particles are currently in the world. This will only work
     * if the emitter type is set to entity.
     *
     * @return how many particles are currently spawned in the world.
     */
    public int getParticleCount() {
        return PARTICLES.size();
    }

    /**
     * Returns the max number of particles allowed at any given time. This will only work
     * if the emitter type is set to entity.
     *
     * @return the max number of particles allowed at and given time in the world.
     */
    public int getMaxParticles() {
        return maxParticles;
    }

    /**
     * @return the chance of a leaf particle being spawned on a block every half a second.
     */
    public double getSpawnChance() {
        return spawnChance;
    }

    @Override
    public void start() {
        // Stop runnables if already started before
        if (updater != null) updater.cancel();
        updater = new BukkitRunnable() { // Auto scan around players for new leaves.
            int ticks = scanDelay;
            int passed = 0;
            @Override
            public void run() {
                if (passed++ >= ticks) {
                    passed = 0;
                    if (!enabled) return;
                    scan();
                    // Reduce the amount of scans being done as more players are online
                    int online = Bukkit.getOnlinePlayers().size();
                    if (online > 20) {
                        ticks = (online / 20) + scanDelay;
                    }
                }
            }
        }.runTaskTimerAsynchronously(PLUGIN, 0, 20);

        startSpawner();
    }

    /**
     * Starts the spawner runnable. If there is already one started then it will be stopped
     * and a new one started in place. The tick rate of this runnable is linked to the emitter
     * type and is restarted by the {@link #updateConfig(ConfigurationSection)} automatically for
     * ant config reloads.
     */
    private void startSpawner() {
        if (spawner != null) spawner.cancel();
        spawner = new BukkitRunnable() {
            @Override
            public void run() {
                tickEntities();
                if (!enabled) return;

                // TODO implement random tick method.
                //randomTickSpawn();

                if (emitterType == EmitterType.ENTITY) {
                    spawnParticlesTick(true, loc -> {
                        WorldData worldData = PLUGIN.getEngine().getWorldProvider().getWorld(loc.getWorld());
                        Material type = loc.getBlock().getRelative(BlockFace.UP).getType();
                        LeafParticle particle = new LeafParticle(loc, PLUGIN.getEngine(), options, type);
                        particle.setForces(worldData.getWind());
                        PARTICLES.add(particle);
                    });
                }
                else if (emitterType == EmitterType.PARTICLE) {
                    spawnParticlesTick(false, loc -> {
                        World world = loc.getWorld();
                        if (world == null) return;
                        int count = MathUtil.randomInt(particleMin, particleMax);
                        double offsetX = MathUtil.randomDouble() / 2;
                        double offsetZ = MathUtil.randomDouble() / 2;
                        world.spawnParticle(particle, loc, count, offsetX, 0, offsetZ, 0);
                    });
                }
            }
        }.runTaskTimerAsynchronously(PLUGIN, emitterType.TICK_RATE, 1);
    }

    /**
     * Future method
     *
     * Uses random ticks to attempt to find a suitable leave block around each player
     * and spawns a leaf particle under it if it does find one.
     *
     * Currently, this method uses about 50% more cpu time with attempts at 50 over the
     * scan method with a range of 12 and fidelity of 3.
     *
     * This is not yet implemented but may be used in future updates as it could provide
     * a more optimized way to work in a larger scan area.
     */
    private void randomTickSpawn() {
        int viewDist = 20;
        int attempts = 50;
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int i = 0; i < attempts; i++) {
                int x = MathUtil.randomInt(-viewDist, viewDist);
                int y = MathUtil.randomInt(-viewDist, viewDist);
                int z = MathUtil.randomInt(-viewDist, viewDist);

                Block block = player.getLocation().getBlock().getRelative(x, y, z);
                Block under = block.getRelative(BlockFace.DOWN);

                if (block.getBlockData() instanceof Leaves && under.isPassable()) {
                    // Summon new particle
                    WorldData data = PLUGIN.getEngine().getWorldProvider().getWorld(block.getWorld());
                    Material type = block.getType();
                    Location loc = under.getLocation();
                    LeafParticle particle = new LeafParticle(loc, PLUGIN.getEngine(), options, type);
                    particle.setForces(data.getWind());
                    PARTICLES.add(particle);
                }
            }
        }
    }

    /**
     * Ticks all leaf entities currently spawned. THis also takes care of removing
     * and particles that are no longer deemed alive.
     */
    private void tickEntities() {
        if (PARTICLES.size() == 0) return;
        Set<LeafParticle> dead = new HashSet<>();
        for (LeafParticle effect : PARTICLES) {
            effect.tick();
            if (!effect.isAlive())
                dead.add(effect);
        }
        PARTICLES.removeAll(dead);
    }

    /**
     * Runs through a loop of all locations executing a task if the chance is lucky.
     *
     * @param offset    if the location should have a random offset applied to it.
     * @param task      task to be executed when the location is lucky.
     */
    private void spawnParticlesTick(boolean offset, IngestedTask<Location> task) {
        for (Location loc : LOCATIONS) {
            if (!MathUtil.chance(spawnChance)) continue;
            double offsetX = 0, offsetZ = 0;
            if (offset) {
                offsetX = MathUtil.randomDouble() - 0.5;
                offsetZ = MathUtil.randomDouble() - 0.5;
            }
            Location spawnLoc = loc.clone().add(offsetX, -0.5, offsetZ);
            task.run(spawnLoc);
        }
    }

    /**
     * Spawns a new leaf particle already assigned to this emitter and returns it.
     *
     * @param loc location to spawn the particle at.
     * @return leaf particle that was spawned.
     */
    public LeafParticle spawn(Location loc) {
        LeafParticle particle = new LeafParticle(loc);
        PARTICLES.add(particle);
        return particle;
    }

    /**
     * Scans an area around all players and checks to see if there are any valid leaves to
     * spawn leafs from.
     */
    public void scan() {
        LOCATIONS.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Ignore afk players.
            UserData userData = PLUGIN.getEngine().getUserData(player.getUniqueId());
            if (userData.getPlayerIdleInfo().isAfk()) continue;

            // Set the location of the player and scan around them.
            SCANNER.setLocation(player.getLocation());
            SCANNER.scan(block -> {
                Block below = block.getRelative(BlockFace.DOWN);
                if (block.getBlockData() instanceof Leaves && below.isPassable() && !below.isLiquid())
                    LOCATIONS.add(block.getLocation().add(0.5, 0, 0.5));
            });
        }
    }

    /**
     * Clears all effects and ends all running tasks.
     */
    @Override
    public void end() {
        if (updater != null) {
            updater.cancel();
            updater = null;
        }
        if (spawner != null) {
            spawner.cancel();
            spawner = null;
        }
        LOCATIONS.clear();
        PARTICLES.forEach(LeafParticle::remove);
        PARTICLES.clear();
    }
}
