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

public class BlockTemperature implements TemperatureModifier {

    private double emission;
    private double scalar;

    /**
     * @param emission defines the emission value for the block.
     * @param scalar   defines the scalar of the blocks emission (how
     *                 quickly the emission falls off).
     */
    public BlockTemperature(double emission, double scalar) {
        this.emission = emission;
        this.scalar = scalar;
    }

    @Override
    public double getEmission() {
        return emission;
    }

    public void setEmission(double emission) {
        this.emission = emission;
    }

    public double getScalar() {
        return scalar;
    }

    public void setScalar(double scalar) {
        this.scalar = scalar;
    }
}
