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

public enum WorldTime {

    MORNING(0, "sunrise"),
    MID_DAY(6000, "day"),
    DUSK(12000, "afternoon"),
    MID_NIGHT(18000, "night");

    private int time;
    private String[] aliases;

    WorldTime(int time, String... aliases) {
        this.time = time;
        this.aliases = aliases;
    }

    public int getTime() {
        return time;
    }

    public static WorldTime find(String name) {
        for (WorldTime time : values()) {
            if (time.name().equalsIgnoreCase(name)) return time;
            for (String s : time.aliases)
                if (s.equalsIgnoreCase(name)) return time;
        }
        return null;
    }
}
