package me.masstrix.eternalnature.util;

public class CuboidScanner {

    private int radius;
    private int x, y, z;
    private CuboidRuntime task;
    private boolean excludeCenter = false;

    public CuboidScanner(int radius, CuboidRuntime task) {
        this(radius, 0, 0, 0, task);
    }

    public CuboidScanner(int radius, int x, int y, int z, CuboidRuntime task) {
        this.radius = Math.abs(radius);
        this.x = x;
        this.y = y;
        this.z = z;
        this.task = task;
    }

    public CuboidScanner excludeCenter() {
        excludeCenter = true;
        return this;
    }

    public final void start() {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (excludeCenter && x == this.x && y == this.y && z == this.z) continue;
                    if (task instanceof CuboidTask)
                        ((CuboidTask) task).run(x + this.x, y + this.y, z + this.z);
                    else if (task instanceof CuboidLocalTask) {
                        ((CuboidLocalTask) task).run(x + this.x, y + this.y, z + this.z, x, y, z);
                    }
                }
            }
        }

        if (task instanceof CuboidTaskCycle)
            ((CuboidTaskCycle) task).finish();
    }

    public interface CuboidRuntime {
    }

    public interface CuboidTask extends CuboidRuntime {
        void run(int x, int y, int z);
    }

    public interface CuboidLocalTask extends CuboidRuntime {
        void run(int x, int y, int z, int localX, int localY, int localZ);
    }

    public interface CuboidTaskCycle extends CuboidTask {
        void finish();
    }
}
