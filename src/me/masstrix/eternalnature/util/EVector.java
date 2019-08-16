package me.masstrix.eternalnature.util;

import java.io.Serializable;

public class EVector extends org.bukkit.util.Vector implements Serializable {

    public EVector() {
        super();
    }

    public EVector(int x, int y, int z) {
        super(x, y, z);
    }

    public EVector(double x, double y, double z) {
        super(x, y, z);
    }

    public EVector(float x, float y, float z) {
        super(x, y, z);
    }
}
