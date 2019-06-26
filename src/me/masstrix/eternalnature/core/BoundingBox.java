package me.masstrix.eternalnature.core;

import org.bukkit.util.Vector;

public class BoundingBox {

    private Vector min, max;

    public BoundingBox(Vector min, Vector max) {
        this.min = min;
        this.max = max;
        validate();
    }

    public void setMin(Vector min) {
        this.min = min;
        validate();
    }

    public void setMax(Vector max) {
        this.max = max;
        validate();
    }

    public Vector getMin() {
        return min;
    }

    public Vector getMax() {
        return max;
    }

    private boolean overlaps(Vector min, Vector max) {
        return this.min.getX() < max.getY() && this.max.getX() > min.getX() && this.min.getY() < max.getY()
                && this.max.getY() > min.getY() && this.min.getZ() < max.getZ() && this.max.getZ() > min.getZ();
    }

    public boolean overlaps(BoundingBox box) {
        return overlaps(box.getMin(), box.getMax());
    }

    public boolean inBounds(Vector v) {
        double x = v.getX(), y = v.getY(), z = v.getZ();
        return x > min.getX() && x < max.getX() && y > min.getY() && y < max.getY() && z > min.getZ() && z < max.getZ();
    }

    @Override
    public int hashCode() {
        return min.hashCode() + max.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BoundingBox
                && ((BoundingBox) obj).min.equals(min)
                && ((BoundingBox) obj).max.equals(max);
    }

    /**
     * Switches any values around to not have the min or max values
     * seem inverted.
     */
    private void validate() {
        if (min == null || max == null) return;
        if (min.getX() > max.getX()) {
            double x = min.getX();
            min.setX(max.getX());
            max.setX(x);
        }
        if (min.getY() > max.getY()) {
            double y = min.getX();
            min.setY(max.getY());
            max.setY(y);
        }
        if (min.getZ() > max.getZ()) {
            double z = min.getZ();
            min.setZ(max.getZ());
            max.setZ(z);
        }
    }
}
