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
import me.masstrix.eternalnature.core.entity.shadow.ShadowArmorStand;
import me.masstrix.eternalnature.core.item.CustomItem;
import me.masstrix.eternalnature.events.LeafSpawnEvent;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.SimplexNoiseOctave;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Objects;

public class LeafParticle extends BaseParticle implements Leaf {

    private SimplexNoiseOctave movementNoise;
    private ShadowArmorStand leaf;
    private double animationOffset;
    private boolean hasSettled;
    // This defines if the particle should float on top of water.
    // Currently unused.
    private boolean willFloat;
    private boolean waitToRestOnGround;
    private double fallRate;
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
        fallRate = MathUtil.randomDouble(0.01, 0.1);
        movementNoise = new SimplexNoiseOctave(MathUtil.randomInt(10000));
        willFloat = MathUtil.chance(0.3);
        waitToRestOnGround = options.forceReachGround;

        loc.setYaw(MathUtil.randomInt(0, 360));
        leaf = new ShadowArmorStand(loc);
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
        if (super.lifeTime-- <= 0 && !waitToRestOnGround) {
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
        boolean wasHasSettled = hasSettled;
        hasSettled = !block.isPassable();
        boolean inWater = loc.getBlock().getType() == Material.WATER;
        if (inWater && !hasSettled && willFloat) {
            // TODO make leaf particles interact with flowing water.
            //      Also make some of them float on the top.
            //      Water direction is client side so that will need to be worked
            //      out to get the flowing direction.
        }

        // If the waitToRestOnGround is set to true and the particle just hit the ground
        // set the particles life to be an appropriate value to last some amount of time
        // on the ground.
        if (!wasHasSettled && hasSettled && waitToRestOnGround && lifeTime < 0) {
            waitToRestOnGround = false;
            while(lifeTime < 0) lifeTime += 15;
        }

        // Burn the particle
        if (loc.getBlock().getType() == Material.LAVA) {
            Levelled levelled = (Levelled) block.getBlockData();
            double y = block.getY() + (levelled.getLevel() / (double) levelled.getMaximumLevel());

            // Remove particle if in the lava
            if (loc.getY() < y) {
                this.alive = false;
                remove();
                return;
            }
        }

        animationOffset += 0.05;

        if (wind != null) {
            Vector force = wind.getForce(loc.getX(), loc.getY(), loc.getZ());
            velocity.add(force);
        }

        velocity.add(new Vector(0, !hasSettled ? -fallRate / 6 : 0, 0));

        if (!hasSettled) {
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
            Vector pose = leaf.getPose(ArmorStandBodyPart.RIGHT_ARM);
            pose.divide(new Vector(1.2, 1.2, 1.2));
            leaf.setPose(ArmorStandBodyPart.RIGHT_ARM, pose);
        }
        velocity.divide(new Vector(1.2, 2, 1.2));
        animationOffset += 0.01;
    }

    private static Location getArmTip(ShadowArmorStand as) {
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
