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

package me.masstrix.eternalnature.core.temperature;

import me.masstrix.eternalnature.util.WorldTime;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class TimeTemperature implements TemperatureModifier {

    public static final int TIME_IN_DAY = 24000;

    private Map<Integer, Double> times = new HashMap<>();

    /**
     * Puts a new value in for the time modifier. If the provided
     * time is invalid it will be ignored.
     *
     * @param time time of day for this modifier. This has to be
     *             between 0 and 24000.
     * @param temp temperature to emit at this time.
     * @return an instance of this TimeTemperature.
     */
    public TimeTemperature put(WorldTime time, double temp) {
        times.put(time.getTime(), temp);
        return this;
    }

    /**
     * Puts a new value in for the time modifier. If the provided
     * time is invalid it will be ignored.
     *
     * @param time time of day for this modifier. This has to be
     *             between 0 and 24000.
     * @param temp temperature to emit at this time.
     * @return an instance of this TimeTemperature.
     */
    public TimeTemperature put(int time, double temp) {
        if (time <= TIME_IN_DAY && time >= 0)
            times.put(time, temp);
        return this;
    }

    @Override
    public double getEmission() {
        return 0;
    }

    /**
     * Gets the worlds temperature based on the modifiers applied
     * for this time modifier.
     *
     * @param world world to get the emission for.
     * @return the emission value for this world.
     */
    public double getLocalTemp(World world) {
        if (times.size() == 0 || world == null) return 0;
        if (times.size() == 1) return times.get(0);
        return getEmission((int) world.getTime());
    }

    /**
     * Returns the temperature based on the from and to points between
     * between time. This will make the values linearly ease between
     * each other.
     *
     * @param time time to get the emission value for.
     * @return the emission value for that time.
     */
    private double getEmission(int time) {
        int from = -1, to = -1;
        int smallest = Integer.MAX_VALUE;
        int largest = Integer.MIN_VALUE;

        for (int v : times.keySet()) {
            if (v == time) {
                from = v;
                to = v;
                break;
            }

            if (v < smallest)
                smallest = v;
            if (v > largest)
                largest = v;

            // From
            if (from == -1 || v < time && time - v < time - from) {
                from = v;
                continue;
            }

            // To
            if (to == -1 || v > time && v - time < to - time) {
                to = v;
            }
        }

        // Update wrapping points.
        if (to < from) to = smallest;
        if (from > time) {
            to = from;
            from = largest;
        }

        // Gets the distance between to and from nodes
        int disFrom= from > time ? (TIME_IN_DAY - from) + time : time - from;
        int disTo = to < from && from <= time ? (TIME_IN_DAY + to) - time : to - time;

        // Gets the max distance value
        double maxDis = disFrom + disTo;

        double percentFrom = disTo / maxDis;
        double percentTo   = disFrom / maxDis;

        return from == to ? times.get(to) : times.get(from) * percentFrom + times.get(to) * percentTo;
    }
}
