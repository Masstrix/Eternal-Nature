package me.masstrix.eternalnature.core.render;

import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class LeafEffect {

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
    public LeafEffect(Location loc) {
        lifeTime = MathUtil.randomInt(60, 120);
        fallRate = MathUtil.random().nextDouble() / 10;

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
    }

    public boolean isAlive() {
        return alive;
    }

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

}
