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

public class EnumUtils {

    /**
     * Finds and returns an exact match in an enum array. If no matching value
     * is in the enum then null is returned.
     *
     * @param enom array of enum values to look through.
     * @param find name of value to find.
     * @param <T> enum type being searched.
     * @return the enum value or null if the value does no exist.
     */
    public static <T extends Enum<?>> T findMatch(T[] enom, String find) {
        return findMatch(enom, find, null);
    }

    /**
     * Finds and returns an exact match in an enum array. If no matching value
     * is in the enum then null is returned.
     *
     * @param enom array of enum values to look through.
     * @param find name of value to find.
     * @param def  default value toi return if no match was found.
     * @param <T> enum type being searched.
     * @return the enum value or null if the value does no exist.
     */
    public static <T extends Enum<?>> T findMatch(T[] enom, String find, T def) {
        find = find.toUpperCase();
        for (T e : enom) {
            if (e.name().equals(find)) return e;
        }
        return def;
    }

    /**
     * Finds and returns the closest matching enum to a given name. The enum value
     * must contain the {@code find} parameter in it to be valid.
     *
     * @param enom array of enum values to look through.
     * @param find name of the value you want to find.
     * @param <T> enum type being searched.
     * @return the closest matching enum value from the array.
     */
    public static <T extends Enum<?>> T findClosestMatch(T[] enom, String find) {
        if (enom == null) return null;
        find = find.toUpperCase();
        T closest = null;
        int closestDiff = -1;
        for (T e : enom) {
            String name = e.name();
            if (!name.contains(find)) continue;
            int diff = StringUtil.distance(name, find);
            if (closest == null || diff < closestDiff) {
                closest = e;
                closestDiff = diff;
            }
            if (closestDiff == 0) break;
        }
        return closest;
    }
}
