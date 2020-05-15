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

package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.EternalWorker;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class Wind implements EternalWorker {

    private EternalNature plugin;
    private WorldData world;
    private SimplexOctaveGenerator xMap;
    private SimplexOctaveGenerator yMap;
    private float fre;
    private float amp;
    private BukkitTask task;

    public Wind(WorldData world, EternalNature plugin, long seed) {
        this.world = world;
        this.plugin = plugin;
        xMap = new SimplexOctaveGenerator(seed, 2);
        xMap.setScale(20);
        xMap.setWScale(2);

        yMap = new SimplexOctaveGenerator(seed + 6543, 2);
        yMap.setScale(20);
        yMap.setWScale(2);
    }

    public Vector getForce(int x, int y) {
        double valX = xMap.noise(x, y, fre, amp);
        double valY = yMap.noise(x, y, fre, amp);
        valX -= 0.5;
        valY -= 0.5;

        double temp = world.getBiomeEmission(x, 0, y);

        return new Vector(valX, temp * 0.01 - 1, valY);
    }

    @Override
    public void start() {
        if (task != null) return;
        this.task = new BukkitRunnable() {
            @Override
            public void run() {

            }
        }.runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void end() {

    }
}
