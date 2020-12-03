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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtil {

    private static Random random = new Random();

    /**
     * Returns an instance of <code>Random</code>. This reduces the use of needing to
     * create new random instances all the time.
     *
     * @return an instance of <code>java.util.Random</code>
     */
    public static Random random() {
        return random;
    }

    /**
     * Gives boolean with the chance. If the chance rolls lower than <i>percent</i>
     * then it will it will be true. Percent values range from 0 to 1.
     *
     * @param percent a percent between 0 and 1.
     * @return true if the roll was within the percentage.
     */
    public static boolean chance(double percent) {
        if (percent >= 1) return true;
        if (percent <= 0) return false;
        return random.nextDouble() <= percent;
    }


    /**
     * Gives boolean from a random chance.
     *
     * @param range a percent between 0 and 1.
     * @return true if the roll was within the percentage.
     */
    public static boolean chance(int range) {
        return randomInt(range) == 1;
    }

    public static double randomDouble() {
        return random.nextDouble();
    }

    /**
     * Returns a random int between 0 and <code>max</code>.
     *
     * @param max max value for random int.
     * @return a random in between 0 and max.
     */
    public static int randomInt(int max) {
        return random.nextInt(max);
    }

    /**
     * Returns a random int within a range.
     *
     * @param min min value.
     * @param max max value.
     * @return a random int between min and max.
     */
    public static int randomInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    /**
     * Returns the difference between two floats.
     *
     * @param a first value.
     * @param b second value.
     * @return the difference between a and b.
     */
    public static float diff(float a, float b) {
        return a > b ? a - b : b - a;
    }

    /**
     * Returns the difference between two doubles.
     *
     * @param a first value.
     * @param b second value.
     * @return the difference between a and b.
     */
    public static double diff(double a, double b) {
        return a > b ? a - b : b - a;
    }

    /**
     * Cubes an int.
     *
     * @param v value to cube.
     * @return v cubed.
     */
    public static int cube(int v) {
        return v * v * v;
    }

    /**
     * Rounds a double value to contain n decimal places.
     *
     * @param value  value to round.
     * @param places how many decimal places ro round to.
     * @return the rounded value or the value if places is less than 0.
     */
    public static double round(double value, int places) {
        if (places < 0) return value;

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Fixes a float value if it is invalid.
     *
     * @param v value to fix.
     * @param fix fixed default value to use if <i>v</i> is invalid.
     * @return returns <i>v</i> if the value is valid otherwise returns <i>fix</i>
     *         if the value is NaN or Infinite.
     */
    public static float fix(float v, float fix) {
        return Float.isNaN(v) || Float.isInfinite(v) ? fix : v;
    }

    /**
     * Fixes a float value if it is invalid.
     *
     * @param v value to fix.
     * @param fix fixed default value to use if <i>v</i> is invalid.
     * @return returns <i>v</i> if the value is valid otherwise returns <i>fix</i>
     *         if the value is NaN or Infinite.
     */
    public static double fix(double v, double fix) {
        return Double.isNaN(v) || Double.isInfinite(v) ? fix : v;
    }

    public static double minMax(double v, double min, double max) {
        return v > max ? max : Math.max(v, min);
    }

    public static float minMax(float v, float min, float max) {
        return v > max ? max : Math.max(v, min);
    }
    public static int minMax(int v, int min, int max) {
        return v > max ? max : Math.max(v, min);
    }
}
