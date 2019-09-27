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

package me.masstrix.eternalnature.core;

import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores an array of points with a height and value to create a gradient between them.
 */
public class HeightGradient {

    private static Map<World.Environment, HeightGradient> defaults = new HashMap<>();

    static {
        HeightGradient normal = new HeightGradient();
        HeightGradient nether = new HeightGradient();

        normal.addPoint(new GradientPoint(0, 10),
                new GradientPoint(20, -5),
                new GradientPoint(50, 0),
                new GradientPoint(70, 0),
                new GradientPoint(255, -10));
        nether.addPoint(new GradientPoint(0, 0),
                new GradientPoint(30, 20),
                new GradientPoint(45, 5),
                new GradientPoint(60, -3),
                new GradientPoint(128, -15));

        defaults.put(World.Environment.NORMAL, normal);
        defaults.put(World.Environment.NETHER, nether);
    }

    /**
     * Returns the default gradient map for that kind of world environment.
     *
     * @param environment environment to get the default gradient for.
     * @return a gradient or null if none exists.
     */
    public static HeightGradient getGradient(World.Environment environment) {
        return defaults.get(environment);
    }

    private GradientPoint min;
    private GradientPoint max;

    private List<GradientPoint> points = new ArrayList<>();

    /**
     * Loads the data from a section of a config file. The section must be formatted as a int:float
     * for key and value.
     *
     * @param section section to load.
     */
    public void load(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            double value = section.getDouble(key, 0);
            if (StringUtil.isInteger(key)) {
                add(new GradientPoint(Integer.parseInt(key), value));
            }
        }
        sort();
    }

    /**
     * Adds point(s) to the array in the gradient. Once added the array of points will be
     * resorted to create a the linear gradient.
     *
     * @param point point(s) to add into the gradient.
     */
    public void addPoint(GradientPoint... point) {
        add(point);
        sort();
    }

    /**
     * @param y y block value to get the modifier for.
     * @return the modifier to apply.
     */
    public double getModifier(int y) {
        if (points.size() == 0) return 0;
        if (y >= max.height) return max.value;
        if (y <= min.height) return min.value;

        GradientPoint min = null, max = null;

        // Find the min and max range points.
        for (GradientPoint point : points) {
            if (point.height == y) return point.value;
            if (max == null) {
                max = point;
            } else if (point.height > y && point.height < max.height) {
                max = point;
            } else {
                min = point;
                break;
            }
        }

        // Mix the min and max values
        if (min != null) {
            double percentTop = ((double) y - min.height) / (max.height - min.height);
            double percentBot = 1D - percentTop;
            double valTop = max.value * percentTop;
            double valBot = min.value * percentBot;
            return valTop + valBot;
        } else {
            return max.value;
        }
    }

    /**
     * Adds new point(s) to the gradient list.
     *
     * @param point point or array of points to add.
     */
    private void add(GradientPoint... point) {
        for (GradientPoint p : point) {
            if (p.height < 0) continue;
            if (min == null || min.height > p.height) {
                min = p;
            }
            if (max == null || max.height < p.height)
                max = p;
            points.add(p);
        }
    }

    /**
     * Sorts the gradient. This puts the highest point first.
     */
    private void sort() {
        points.sort((a, b) -> Integer.compare(b.height, a.height));
    }

    public static class GradientPoint {

        private double value;
        private int height;

        public GradientPoint(int height, double value) {
            this.height = height;
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public int getHeight() {
            return height;
        }

        @Override
        public String toString() {
            return height + ":" + value;
        }
    }
}
