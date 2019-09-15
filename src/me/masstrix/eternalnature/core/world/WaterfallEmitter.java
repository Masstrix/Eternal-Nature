package me.masstrix.eternalnature.core.world;

import org.bukkit.*;

import java.io.*;
import java.util.UUID;

public class WaterfallEmitter implements Serializable {

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
        if (location == null) return;
        Location loc = location.clone().add(0.5, 0, 0.5);
        loc.getWorld().spawnParticle(Particle.SPIT, loc.clone().add(0, 0.5, 0), 1, 0.5, 0, 0.5, 0.05);
        loc.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 5, 0.5, 0, 0.5, 0.05);
    }

    public boolean isValid() {
        return location != null && location.clone().add(0, 1, 0).getBlock().getType() == Material.WATER;
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof WaterfallEmitter && o.hashCode() == this.hashCode();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(height);
        out.writeUTF(location.getWorld().getUID().toString());
        out.writeDouble(location.getX());
        out.writeDouble(location.getY());
        out.writeDouble(location.getZ());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.height = in.readInt();
        World world = Bukkit.getWorld(UUID.fromString(in.readUTF()));
        if (world == null)
            return;
        this.location = new Location(world, in.readDouble(), in.readDouble(), in.readDouble());
    }

//     * private void writeObject(java.io.ObjectOutputStream out)
// *     throws IOException
// * private void readObject(java.io.ObjectInputStream in)
// *     throws IOException, ClassNotFoundException;
// * private void readObjectNoData()
// *     throws ObjectStreamException;
}
