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

package me.masstrix.eternalnature.util;

public class CuboidScanner {

    private int radius;
    private int x, y, z;
    private CuboidRuntime task;
    private boolean excludeCenter = false;
    private boolean center = true;

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

    public CuboidScanner center(boolean b) {
        this.center = b;
        return this;
    }

    public CuboidScanner excludeCenter() {
        excludeCenter = true;
        return this;
    }

    public final void start() {
        if (radius <= 0) {
            if (task instanceof CuboidTask)
                ((CuboidTask) task).run(x, y, z);
            else if (task instanceof CuboidLocalTask) {
                ((CuboidLocalTask) task).run(x, y, z, x, y, z);
            }
            return;
        }
        int start = center ? -radius : 0;
        for (int y = start; y <= radius; y++) {
            for (int x = start; x <= radius; x++) {
                for (int z = start; z <= radius; z++) {
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
