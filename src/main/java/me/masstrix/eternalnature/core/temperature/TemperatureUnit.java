/*
 * Copyright 2021 Matthew Denton
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

/**
 * A constant for each temperature unit (celsius, fahrenheit and kelvin) for simple
 * conversion between them.
 */
public enum TemperatureUnit {

    CELSIUS {
        @Override
        public float toFahrenheit(float val) {
            return (val * 9 / 5) + 32;
        }

        @Override
        public float toKelvin(float val) {
            return val - 273.15F;
        }
    },
    FAHRENHEIT {
        @Override
        public float toCelsius(float val) {
            return (val - 32) * 9 / 5;
        }

        @Override
        public float toKelvin(float val) {
            return (val - 32) * 5 / 9 + 273.15F;
        }
    },
    KELVIN {
        @Override
        public float toCelsius(float val) {
            return val + 273.15F;
        }

        @Override
        public float toFahrenheit(float val) {
            return (val - 273.15F) * 9 / 5 + 32;
        }
    };

    /**
     * Converts this unit to celsius.
     *
     * @param val value to convert.
     * @return the value in celsius.
     */
    public float toCelsius(float val) {
        return val;
    }

    /**
     * Converts this unit to fahrenheit.
     *
     * @param val value to convert.
     * @return the value in fahrenheit.
     */
    public float toFahrenheit(float val) {
        return val;
    }

    /**
     * Converts this unit to kelvin.
     *
     * @param val value to convert.
     * @return the value in kelvin.
     */
    public float toKelvin(float val) {
        return val;
    }
}
