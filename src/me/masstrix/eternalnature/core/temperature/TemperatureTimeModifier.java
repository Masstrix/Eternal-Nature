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

package me.masstrix.eternalnature.core.temperature;

import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class TemperatureTimeModifier implements TemperatureModifier {

    private Map<Long, Double> mappings = new HashMap<>();
    private World.Environment environment;
    private World world;

    public TemperatureTimeModifier(World world) {
        this.world = world;
        this.environment = world.getEnvironment();
    }

    public void set(long time, double modifier) {

    }

    @Override
    public double getTemperature(int x, int y, int z) {
        long time = world.getTime();
        return 0;
    }
}
