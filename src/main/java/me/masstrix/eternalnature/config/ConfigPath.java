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

package me.masstrix.eternalnature.config;

public class ConfigPath {

    public static final String GENERAL = "general";
    public static final String UPDATE_CHECK = path(GENERAL, "check-for-updates");
    public static final String UPDATE_NOTIFY = path(GENERAL, "notify-update-join");
    public static final String LANGUAGE = path(GENERAL, "language");

    public static final String GLOBAL = "global";
    public static final String LEAF_EFFECT = path(GLOBAL, "falling-leaves");
    public static final String LEAF_EFFECT_ENABLED = path(LEAF_EFFECT, "enabled");
    public static final String LEAF_EFFECT_RANGE = path(LEAF_EFFECT, "range");
    public static final String LEAF_EFFECT_FIDELITY = path(LEAF_EFFECT, "fidelity");
    public static final String LEAF_EFFECT_MAX_PARTICLES = path(LEAF_EFFECT, "max-particles");
    public static final String LEAF_EFFECT_DELAY = path(LEAF_EFFECT, "scan-delay");
    public static final String LEAF_EFFECT_CHANCE = path(LEAF_EFFECT, "chance");

    public static final String RAND_SPREAD = path(GLOBAL, "randomly-spread-trees");
    public static final String RAN_SPREAD_ENABLED = path(RAND_SPREAD, "enabled");
    public static final String RAN_SPREAD_RANGE = path(RAND_SPREAD, "range");
    public static final String RAN_SPREAD_SCANS = path(RAND_SPREAD, "scans");

    public static final String AGE_ITEMS = path(GLOBAL, "age-items");

    public static final String AUTO_PLANT = path(GLOBAL, "auto-plant");
    public static final String AUTO_PLANT_ENABLED = path(AUTO_PLANT, "enabled");
    public static final String AUTO_PLANT_PLAY_SOUND = path(AUTO_PLANT, "play-sound");
    public static final String AUTO_PLANT_CROPS = path(AUTO_PLANT, "replant-crops");


    private static String path(String from, String... to) {
        StringBuilder builder = new StringBuilder(from);
        for (String s : to) {
            if (builder.length() > 0) builder.append(".");
            builder.append(s);
        }
        return builder.toString();
    }
}
