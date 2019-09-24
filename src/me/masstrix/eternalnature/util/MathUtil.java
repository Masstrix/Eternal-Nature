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

import java.util.Random;

public class MathUtil {

    private static Random random = new Random();

    public static Random random() {
        return random;
    }

    public static boolean chance(double percent) {
        if (percent >= 1) return true;
        return random.nextDouble() <= percent;
    }

    public static boolean chance(int val) {
        return randomInt(val) == 1;
    }

    public static int randomInt(int max) {
        return random.nextInt(max);
    }

    public static int randomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    public static float diff(float a, float b) {
        return a > b ? a - b : b - a;
    }

    public static int cube(int v) {
        return v * v * v;
    }
}
