package me.masstrix.eternalnature.core.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;

public class WaterfallEmitter {

    private Location location;
    private int height;

    public WaterfallEmitter(Location loc, int height) {
        this.location = loc;
        this.height = height;
    }

    public Location getLocation() {
        return location;
    }

    public int getHeight() {
        return height;
    }

    public void tick() {
        Location loc = location.clone().add(0.5, 0, 0.5);
        loc.getWorld().spawnParticle(Particle.SPIT, loc.clone().add(0, 0.5, 0), 1, 0.5, 0, 0.5, 0.05);
        loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 5, 0.5, 0, 0.5, 0.05);
    }

    public boolean isValid() {
        return location.clone().add(0, 0.5, 0).getBlock().getType() == Material.WATER;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WaterfallEmitter && o.hashCode() == this.hashCode();
    }
}
