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

package me.masstrix.eternalnature.util;

import java.util.Random;

public class SimplexNoise {

    private SimplexNoiseOctave[] octaves;
    private double[] frequencys;
    private double[] amplitudes;

    private double persistence;
    private int seed;

    public SimplexNoise(int seed) {
        this(1, 1, seed);
    }

    public SimplexNoise(int largestFeature, double persistence, int seed) {
        this.persistence = persistence;
        this.seed = seed;

        // receives a number (eg 128) and calculates what power of 2 it is (eg 2^7)
        int numberOfOctaves = (int) Math.ceil(Math.log10(largestFeature) / Math.log10(2));

        octaves = new SimplexNoiseOctave[numberOfOctaves];
        frequencys = new double[numberOfOctaves];
        amplitudes = new double[numberOfOctaves];

        Random rnd = new Random(seed);

        for (int i = 0; i < numberOfOctaves; i++) {
            octaves[i] = new SimplexNoiseOctave(rnd.nextInt());
            frequencys[i] = Math.pow(2, i);
            amplitudes[i] = Math.pow(persistence, octaves.length - i);
        }
    }

    /**
     * @return the seed used for this noise.
     */
    public int getSeed() {
        return seed;
    }

    public double getNoise(int x) {
        double result = 0;
        for (int i = 0; i < octaves.length; i++) {
            result += octaves[i].noise(x / frequencys[i]) * amplitudes[i];
        }
        return result;
    }

    public double getNoise(int x, int y) {
        double result = 0;
        for (int i = 0; i < octaves.length; i++) {
            result = result + octaves[i].noise(x / frequencys[i], y / frequencys[i]) * amplitudes[i];
        }
        return result;
    }

    public double getNoise(int x, int y, int z) {
        double result = 0;
        for (int i = 0; i < octaves.length; i++) {
            double frequency = Math.pow(2, i);
            double amplitude = Math.pow(persistence, octaves.length - i);

            result = result + octaves[i].noise(x / frequency, y / frequency, z / frequency) * amplitude;
        }
        return result;
    }
}