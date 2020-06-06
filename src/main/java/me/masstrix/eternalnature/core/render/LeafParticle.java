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

package me.masstrix.eternalnature.core.render;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.api.Leaf;
import me.masstrix.eternalnature.core.CleanableEntity;
import me.masstrix.eternalnature.core.EntityCleanup;
import me.masstrix.eternalnature.core.entity.CachedEntity;
import me.masstrix.eternalnature.core.entity.EntityOption;
import me.masstrix.eternalnature.core.entity.EntityStorage;
import me.masstrix.eternalnature.core.item.CustomItem;
import me.masstrix.eternalnature.events.LeafSpawnEvent;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.EulerAngle;

import java.util.logging.Level;

public class LeafParticle extends CachedEntity implements CleanableEntity, Leaf {

    private ArmorStand leaf;
    private boolean alive;
    private int lifeTime;
    private int ticks;
    private double fallRate;

    /**
     * Creates a new leaf effect. Leaves when ticked will slowly fall until they hit a
     * non passable block or there lifetime has ended.
     *
     * @param loc location to spawn the effect at.
     */
    public LeafParticle(EntityStorage storage, EternalNature plugin, Location loc) {
        super(storage);
        super.options(EntityOption.REMOVE_ON_RESTART);
        lifeTime = MathUtil.randomInt(60, 120);
        fallRate = MathUtil.random().nextDouble() / 10;

        LeafSpawnEvent event = new LeafSpawnEvent(this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        leaf = loc.getWorld().spawn(loc.add(0, -0.5, 0), ArmorStand.class, a -> {
            a.setMarker(true);
            a.setSmall(true);
            a.setSmall(true);
            a.setSilent(true);
            a.setVisible(false);
            a.setGravity(false);
            a.getEquipment().setItemInMainHand(CustomItem.LEAF.get());
            a.setRightArmPose(new EulerAngle(
                    degreesToEuler(MathUtil.randomInt(90)),
                    degreesToEuler(MathUtil.randomInt(90)),
                    degreesToEuler(MathUtil.randomInt(90))));
        });
        alive = true;

        // Add current session id to entity to keep it living if a flush is run.
        leaf.setMetadata("session", new FixedMetadataValue(plugin, EntityStorage.SESSION_ID.hashCode()));

        super.setWorld(loc.getWorld().getUID());
        super.setEntityId(leaf.getUniqueId());

        // Cleans up the entity at start and stop of plugin
        new EntityCleanup(plugin, this);
        super.cache();
    }

    @Override
    public boolean hasSettled() {
        return false;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void remove() {
        alive = false;
        leaf.remove();
        getStorage().remove(this);
    }

    public void tick() {
        if (lifeTime-- <= 0 || ticks++ > 20 && !leaf.getLocation().clone().add(0, 1, 0).getBlock().isPassable()) {
            remove(); // End the effect if leaf hits the ground
            lifeTime = 0;
        }

        double x, z;
        x = MathUtil.random().nextDouble() / 20;
        z = MathUtil.random().nextDouble() / 20;
        leaf.setRightArmPose(leaf.getRightArmPose().add(updateAngle(), updateAngle(), updateAngle()));
        leaf.teleport(leaf.getLocation().add(x, -fallRate, z));

    }

    private double updateAngle() {
        return degreesToEuler(MathUtil.chance(2) ? -MathUtil.random().nextInt(3)
                : MathUtil.random().nextInt(3));
    }

    /**
     * Converts an angle in degrees to euler.
     *
     * @param v angle in degrees.
     * @return the angle as a euler unit.
     */
    private double degreesToEuler(double v) {
        return (v / 180) * Math.PI;
    }

    @Override
    public Entity[] getEntities() {
        return new Entity[] {leaf};
    }

    /**
     * Removes entities based on what they are holding and the options set on the armor stand.
     * This is a dirty method of removing them if there are any issues of leaf particles being
     * glitched.
     *
     * @return how many entities were removed.
     */
    public static int removeBrokenParticles() {
        EternalNature.getPlugin(EternalNature.class).getLogger().log(Level.INFO,
                "Doing dirty cleaning of potentially glitched leaf effects...");
        int countCleaned = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (removeIfMatches(e)) {
                    e.remove();
                    countCleaned++;
                }
            }
        }
        return countCleaned;
    }

    private static boolean removeIfMatches(Entity entity) {
        if (!(entity instanceof ArmorStand)) return false;
        ArmorStand stand = (ArmorStand) entity;
        if (stand.hasMetadata("session") &&
                stand.getMetadata("session").get(0).asInt()
                        == EntityStorage.SESSION_ID.hashCode())
            return false;
        return stand.isMarker() && !stand.hasGravity()
                && stand.getItemInHand().getType() == Material.KELP;
    }
}
