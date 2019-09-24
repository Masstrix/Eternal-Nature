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
