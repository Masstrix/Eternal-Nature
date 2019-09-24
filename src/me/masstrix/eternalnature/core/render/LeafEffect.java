package me.masstrix.eternalnature.core.render;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.api.Leaf;
import me.masstrix.eternalnature.core.CleanableEntity;
import me.masstrix.eternalnature.core.EntityCleanup;
import me.masstrix.eternalnature.events.LeafSpawnEvent;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.EulerAngle;

public class LeafEffect implements CleanableEntity, Leaf {

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
    public LeafEffect(EternalNature plugin, Location loc) {
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
            a.setItemInHand(new ItemStack(Material.KELP));
            a.setRightArmPose(new EulerAngle(
                    getAngle(MathUtil.randomInt(90)),
                    getAngle(MathUtil.randomInt(90)),
                    getAngle(MathUtil.randomInt(90))));
        });
        alive = true;
        // Cleans up the entity at start and stop of plugin
        new EntityCleanup(plugin, this);
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
        return getAngle(MathUtil.chance(2) ? -MathUtil.random().nextInt(3)
                : MathUtil.random().nextInt(3));
    }

    private double getAngle(double v) {
        return (v / 180) * Math.PI;
    }

    @Override
    public Entity[] getEntities() {
        return new Entity[] {leaf};
    }
}
