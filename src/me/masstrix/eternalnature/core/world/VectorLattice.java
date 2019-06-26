package me.masstrix.eternalnature.core.world;

import org.bukkit.util.Vector;

public class VectorLattice {

    private Vector[][][] points;

    public VectorLattice(int size) {
        points = new Vector[size][size][size];
    }

    public void set(int x, int y, int z, Vector vec) {
        validateValues(x, y, z);
        this.points[x][y][z] = vec;
    }

    public Vector get(int x, int y, int z) {
        validateValues(x, y, z);
        return points[x][y][z];
    }

    private void validateValues(int x, int y, int z) {
        if (x < 0) throw new IllegalArgumentException("x value is negative.");
        else if (y < 0) throw new IllegalArgumentException("y value is negative.");
        else if (z < 0) throw new IllegalArgumentException("z value is negative.");
    }
}
