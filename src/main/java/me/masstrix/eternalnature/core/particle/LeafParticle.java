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

package me.masstrix.eternalnature.core.particle;

import me.masstrix.eternalnature.EternalEngine;
import me.masstrix.eternalnature.api.Leaf;
import me.masstrix.eternalnature.core.entity.shadow.ArmorStandBodyPart;
import me.masstrix.eternalnature.core.entity.shadow.ItemSlot;
import me.masstrix.eternalnature.core.entity.shadow.ShaArmorStand;
import me.masstrix.eternalnature.core.item.CustomItem;
import me.masstrix.eternalnature.events.LeafSpawnEvent;
import me.masstrix.eternalnature.util.Direction;
import me.masstrix.eternalnature.util.LiquidFlow;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.SimplexNoiseOctave;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Objects;

public class LeafParticle extends BaseParticle implements Leaf {

    private SimplexNoiseOctave movementNoise;
    private ShaArmorStand leaf;
    private double animationOffset;
    private boolean hasSettled, wasOnGround;
    // This defines if the particle should float on top of water.
    // Currently unused.
    private boolean willFloat;
    private boolean waitToRestOnGround;
    private double fallRate;
    private int maxLifetime = 20 * 30;
    private int ticksNotInAir = 0;
    private int maxTicksNotInAir = 20 * 3;
    private final double ARM_OFFSET = degreesToEuler(MathUtil.randomDouble() * 360);

    /**
     * Creates a new leaf effect. Leaves when ticked will slowly fall until they hit a
     * non passable block or there lifetime has ended.
     *
     * @param loc location to spawn the effect at.
     */
    public LeafParticle(Location loc) {
        this(loc, null);
    }

    /**
     * Creates a new leaf effect. Leaves when ticked will slowly fall until they hit a
     * non passable block or there lifetime has ended. Using this method will also call
     * the {@link LeafSpawnEvent}.
     *
     * @param loc    location to spawn the effect at.
     * @param engine engine for the plugin, when this is set wind forces will be applied
     *               to the effect.
     */
    public LeafParticle(Location loc, EternalEngine engine) {
        this(loc, engine, LeafOptions.DEFAULT);
    }

    /**
     * Creates a new leaf particle. The particle uses the base of a {@link BaseParticle} to create an
     * armor stand holding a kelp item. If spawned natrually then the particle will be ticked by the
     * {@link me.masstrix.eternalnature.core.world.LeafEmitter} to slowly fall down.
     *
     * @param loc     location to spawn the particle from.
     * @param engine  an instance of the eternal engine.
     * @param options options for spawning the particle.
     */
    public LeafParticle(Location loc, EternalEngine engine, LeafOptions options) {
        if (loc == null) return;
        if (engine != null) {
            // Call the spawn ever.
            Bukkit.getScheduler().callSyncMethod(engine.getPlugin(), () -> {
                LeafSpawnEvent event = new LeafSpawnEvent(this);
                Bukkit.getPluginManager().callEvent(event);
                return true;
            });
        }
        lifeTime = MathUtil.randomInt(options.lifeMin, options.lifeMax);
        fallRate = MathUtil.randomDouble(0.04, 0.1);
        movementNoise = new SimplexNoiseOctave(MathUtil.randomInt(10000));
        waitToRestOnGround = options.forceReachGround;

        // TODO change this to a tick based system that ticks down and sinks after it reaches 0.
        //      this should start with a random number with a 20% chance to start at 0
        willFloat = MathUtil.chance(0.8);

        loc.setYaw(MathUtil.randomInt(0, 360));
        leaf = new ShaArmorStand(loc);
        leaf.setSmall(true);
        leaf.setMarker(true);
        leaf.setArms(true);
        leaf.setInvisible(true);
        leaf.setSlot(ItemSlot.MAINHAND, CustomItem.LEAF.get());

        // Sets how far away a player has to be to see the particle.
        // This will not be sent to a player if they suddenly become in range of it.
        int renderDistance = 32;

        int distSq = renderDistance * renderDistance;
        for (Player player : Objects.requireNonNull(loc.getWorld()).getPlayers()) {
            if (loc.distanceSquared(player.getLocation()) < distSq) {
                leaf.sendTo(player);
            }
        }
        alive = true;
    }

    @Override
    public boolean hasSettled() {
        return hasSettled;
    }

    @Override
    public void remove() {
        super.remove();
        leaf.remove();
    }

    @Override
    public Location getLocation() {
        return getArmTip(leaf);
    }

    @Override
    public void tick() {
        // Remove the particle if it's lifetime is reached
        if ((super.lifeTime-- <= 0 && !waitToRestOnGround)
                || ticksLived++ > maxLifetime
                || ticksNotInAir > maxTicksNotInAir) {
            remove();
            super.lifeTime = 0;
            return;
        }

        Location loc = getLocation();
        if (loc == null) {
            remove();
            return;
        }

        Block block = loc.getBlock();
        boolean inWater = block.isLiquid();

        // Defines the height of the block the entity is currently above.
        double blockHeight = 1;
        BoundingBox blockBounds = block.getBoundingBox();
        blockHeight = block.getY() + blockBounds.getHeight();

        // Tick the not int air ticks if it's not otherwise reset them.
        if (hasSettled || inWater) ticksNotInAir++;
        else ticksNotInAir = 0;

        // Add velocity in the direction of the waters flow
        if (inWater) {
            // TODO make leaf particle sink slowly if it is sinking / not floating
            // TODO make particles float on top of water. Currently they die instantly when going in source blocks

            Levelled data = (Levelled) block.getBlockData();
            int waterLevel = data.getMaximumLevel() - data.getLevel();
            double waterHeight = data.getLevel() == 0 ? 0 : data.getLevel() / (double) data.getMaximumLevel();

            blockHeight -= waterHeight;

            // Add water flow velocity
            Direction flow = LiquidFlow.getFlowDir(block, false);
            Vector flowAdd = flow.asVector().normalize().multiply(0.01);
            flowAdd.multiply(waterLevel);
            flowAdd.multiply(0.2);
            velocity.add(flowAdd);

            // Water buoyancy if on/in water
            if (loc.getY() <= blockHeight + 0.05)
                velocity.add(new Vector(0, 0.05, 0));
        }

        hasSettled = loc.getY() <= blockHeight;

        // If the waitToRestOnGround is set to true and the particle just hit the ground
        // set the particles life to be an appropriate value to last some amount of time
        // on the ground.
        if (!wasOnGround && hasSettled && waitToRestOnGround && lifeTime < 0) {
            waitToRestOnGround = false;
            while(lifeTime < 0) lifeTime += 40;
        }

        // Burn the particle
        Material bt = block.getType();
        if (bt == Material.LAVA || bt == Material.FIRE || bt == Material.SOUL_FIRE) {
            // Remove particle if in the lava
            if (loc.getY() <= blockHeight) {
                this.alive = false;
                remove();
                return;
            }
        }

        animationOffset += 0.05;

        // Add wind to the leaf's velocity if a wind system is attached to it.
        if (wind != null) {
            // FIXME readd
            //velocity.add(wind.apply(velocity, loc.getX(), loc.getY(), loc.getZ()));
        }

        // Gravity
        velocity.add(new Vector(0, !hasSettled ? -fallRate / 6 : 0, 0));

        if (!hasSettled) {
            // Random floating noise
            velocity.add(new Vector(
                    movementNoise.noise(animationOffset) * 0.01,
                    0,
                    movementNoise.noise(animationOffset + 50) * 0.01));
            leaf.move(velocity.getX(), velocity.getY(), velocity.getZ());
            double poseX = movementNoise.noise(animationOffset) * 0.5;
            double poseY = movementNoise.noise(animationOffset) * 10;
            double poseZ = movementNoise.noise(animationOffset) * 10;
            leaf.setPose(ArmorStandBodyPart.RIGHT_ARM, new Vector(poseX + ARM_OFFSET, poseY, poseZ));
        } else {
            // Hand animations for side to side floating animation
            Vector pose = leaf.getPose(ArmorStandBodyPart.RIGHT_ARM);
            pose.divide(new Vector(1.2, 1.2, 1.2));
            leaf.setPose(ArmorStandBodyPart.RIGHT_ARM, pose);
        }
        velocity.divide(new Vector(1.2, 2, 1.2));
        animationOffset += 0.01;
        wasOnGround = hasSettled;
    }

    private static Location getArmTip(ShaArmorStand as) {
        if (as == null) return null;

        float offsetY = as.isSmall() ? 11f : 22f;
        float offsetShoulder = as.isSmall() ? 3f : 5f;
        float armLength = as.isSmall() ? 5f : 10f;

        // Gets shoulder location
        Location asl = as.getLocation().clone();
        asl.setYaw(asl.getYaw() + 90f);
        Vector dir = asl.getDirection();
        asl.setX(asl.getX() + offsetShoulder / 16f * dir.getX());
        asl.setY(asl.getY() + offsetY / 16f);
        asl.setZ(asl.getZ() + offsetShoulder / 16f * dir.getZ());

        // Get Hand Location
        Vector pose = as.getPose(ArmorStandBodyPart.RIGHT_ARM);
        if (pose == null) {
            return null;
        }
        EulerAngle ea = new EulerAngle(
                Math.toRadians(pose.getX()),
                Math.toRadians(pose.getY()),
                Math.toRadians(pose.getZ()));
        Vector armDir = getDirection(ea.getY(), ea.getX(), -ea.getZ());
        armDir = armDir.rotateAroundY(Math.toRadians(asl.getYaw() - 90f));
        armDir.rotateAroundY(Math.toRadians(asl.getYaw() - 90F));
        asl.setX(asl.getX() + armLength / 16f * armDir.getX());
        asl.setY(asl.getY() + armLength / 16f * armDir.getY());
        return asl;
    }

    private static Vector getDirection(double yaw, double pitch, double roll) {
        Vector v = new Vector(0, -1, 0);
        v.rotateAroundX(pitch);
        v.rotateAroundY(yaw);
        v.rotateAroundZ(roll);
        return v;
    }

    /**
     * Stores the options fpr a leaf particle for when a new particle is spawned. This allows for
     * more control on how the particle should function as it is alive.
     */
    public static class LeafOptions {
        public static final LeafOptions DEFAULT = new LeafOptions()
                .setLifeMax(150)
                .setLifeMin(120)
                .setForceReachGround(false);

        private int lifeMax;
        private int lifeMin;
        private boolean forceReachGround;

        public int getLifeMax() {
            return lifeMax;
        }

        public LeafOptions setLifeMax(int lifeMax) {
            this.lifeMax = lifeMax;
            return this;
        }

        public int getLifeMin() {
            return lifeMin;
        }

        public LeafOptions setLifeMin(int lifeMin) {
            this.lifeMin = lifeMin;
            return this;
        }

        public boolean isForceReachGround() {
            return forceReachGround;
        }

        public LeafOptions setForceReachGround(boolean forceReachGround) {
            this.forceReachGround = forceReachGround;
            return this;
        }
    }
}
