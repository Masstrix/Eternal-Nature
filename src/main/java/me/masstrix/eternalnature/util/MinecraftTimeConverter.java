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

import org.bukkit.World;

public class MinecraftTimeConverter {

    private static final long TICKS_PER_DAY = 24000;
    private static final long MIDNIGHT = 18000;
    private static final long MIDDAY = 6000;
    private boolean midnightReset;

    public MinecraftTimeConverter(boolean midnightReset) {
        this.midnightReset = midnightReset;
    }

    public long convert(World world) {
        long time = world.getTime();
        if (midnightReset) {
            time += MIDNIGHT;

        }
        return time;
    }

    public String ampm(World world) {
        return convert(world) < 12000 ? "am" : "pm";
    }
}
