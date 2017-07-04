package com.astronstudios.natrual;

import java.util.HashMap;
import java.util.Map;

public class TemperatureMap {

    private Height height;

    public TemperatureMap() {
        height = new Height();
    }

    public Height getHeight() {
        return height;
    }

    class Height {
        private Map<Double, Double> temps = new HashMap<>();

        Height() {
            temps.put(0D, 20D);
            temps.put(20D, 0D);
            temps.put(40D, -5D);
            temps.put(60D, 10D);
            temps.put(70D, 12D);
            temps.put(100D, 20D);
        }

        public double get(double height) {
            if (temps.containsKey(height)) return temps.get(height);
            double lower = 0, upper = 0;

            for (double i : temps.keySet()) {
                if (i <= height && upper - height > i - height) {
                    upper = temps.get(i);
                }
            }

            return 0D;
        }
    }

    class TemperatureGradent {

    }
}
