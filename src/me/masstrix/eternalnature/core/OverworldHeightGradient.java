package me.masstrix.eternalnature.core;

import java.util.HashMap;
import java.util.Map;

public class OverworldHeightGradient {

    private Map<Integer, Float> points = new HashMap<>();
    private Map<Integer, Float> gradient = new HashMap<>();

    {
        points.put(250, -15F);
        points.put(70, 0F);
        points.put(60, 0F);
        points.put(50, 2F);
        points.put(0, 15F);

        for (int i = 0; i < 250; i++) {

            float min = 0, max = 0;
            int minY = 0, maxY = 250;

            // Get min and max points for this y value
            for (int p : points.keySet()) {
                if (p > i && p < maxY) {
                    max = points.get(p);
                }
                else if (p < i && p > minY) {
                    min = points.get(p);
                }
            }

            int height = maxY - minY;
            float n = (float) i / (float) height;

            gradient.put(i, n * max + ((1 - n) * min));
        }
    }

    public float getValue(int y) {
        return gradient.get(y > 250 ? 250 : y < 0 ? 0 : y);
    }
}
