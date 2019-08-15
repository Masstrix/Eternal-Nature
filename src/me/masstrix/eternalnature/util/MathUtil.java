package me.masstrix.eternalnature.util;

import java.util.Random;

public class MathUtil {

    private static Random random = new Random();

    public static Random random() {
        return random;
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
