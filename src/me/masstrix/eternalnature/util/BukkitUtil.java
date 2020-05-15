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

import org.bukkit.Material;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;

public class BukkitUtil {

    public static Material findMtl(String name) {
        for (Material m : Material.values()) {
            if (m.name().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    /**
     * Finds the closest matching biome to the provided name.
     *
     * @param name  Name of the biome to search for.
     * @param exact If it should return the closest matching biome or
     *              only an exact biome match. Both are not case-sensitive.
     * @return the closest found biome.
     */
    public static Biome findBiome(String name, boolean exact) {
        name = name.toUpperCase();
        int dis = Integer.MAX_VALUE;
        Biome biome = null;
        for (Biome b : Biome.values()) {
            if (b.name().equals(name) && exact) return b;
            if (exact) continue;
            int d = StringUtil.distance(b.name(), name);

            if (d > dis) continue;

            dis = d;
            biome = b;
        }
        return biome;
    }

    /**
     * Finds the closest matching biome to the provided name.
     *
     * @param name  Name of the biome to search for.
     * @return all biomes with a matching name.
     */
    public static Biome[] findBiomes(String name) {
        name = name.toUpperCase();
        List<Biome> found = new ArrayList<>();
        for (Biome b : Biome.values()) {
            if (b.name().contains(name)) found.add(b);
        }
        return (Biome[]) found.toArray();
    }
}
