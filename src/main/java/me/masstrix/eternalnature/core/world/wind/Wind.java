/*
 * Copyright 2020 Matthew Denton
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

package me.masstrix.eternalnature.core.world.wind;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.Ticking;
import me.masstrix.eternalnature.core.world.WorldData;
import org.bukkit.util.Vector;
import org.bukkit.util.noise.SimplexOctaveGenerator;

public class Wind implements Ticking {

    private EternalNature plugin;
    private WorldData world;
    private SimplexOctaveGenerator xMap;
    private SimplexOctaveGenerator yMap;
    private float fre = 0.03F;
    private float amp = 0.01F;
    private double offset;
    private byte offsetDir;
    private double ticks = 0;

    public Wind(WorldData world, EternalNature plugin, long seed) {
        this.world = world;
        this.plugin = plugin;
        xMap = new SimplexOctaveGenerator(seed, 2);
        xMap.setScale(0.01);
        xMap.setWScale(2);

        yMap = new SimplexOctaveGenerator(seed + 6543, 2);
        yMap.setScale(0.01);
        yMap.setWScale(2);
    }

    public Wind setFrequency(float fre) {
        this.fre = fre;
        return this;
    }

    public Wind setAmplitude(float amp) {
        this.amp = amp;
        return this;
    }

    public Vector getForce(int x, int z) {
        return this.getForce((double) x, z);
    }

    public Vector getForce(double x, double z) {
        double valX = xMap.noise(x, offset, z, fre, amp);
        double valZ = xMap.noise(x, offset, z, fre, amp);

        double cos = Math.cos(valX);
        double sin = Math.sin(valZ);

        double temp = world.getBiomeEmission((int) x, 0, (int) z);
        double pseudoUpdraft = temp * 0.001;
        double strength = xMap.noise((x / 10) + (ticks / 10), ticks, (z / 10) + (ticks / 10),
                0.0001, 0.001);
        strength = Math.max(0, strength * 0.009);

        double gust = xMap.noise(ticks, 0.1, 1);
        double gustStrength = Math.max((gust - 0.6) * 0.05, 0);

        return new Vector(cos, 0, sin)
                .rotateAroundZ(pseudoUpdraft)
                .normalize()
                .multiply(strength + gustStrength);
    }

    @Override
    public void tick() {
        int range = 10000;
        offset += offsetDir < 0 ? -0.5 : 0.5;
        if (offset >= range && offsetDir > 0) {
            offsetDir = -1;
        } else if (offset < range && offsetDir < 0) {
            offsetDir = 1;
        }
        ticks += 0.1;
    }
}
