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

import java.util.Collection;
import java.util.List;

public class ListUtils {

    /**
     * Finds and returns a matching value in a list.
     *
     * @param list list of string values to look through.
     * @param find name of value to find.
     * @return the string value or null if the value does no exist.
     */
    public static String findMatch(Collection<String> list, String find, boolean ignoreCase) {
        find = find.toUpperCase();
        for (String e : list) {
            if (ignoreCase ? e.equalsIgnoreCase(find) : e.equals(find)) return e;
        }
        return null;
    }

    /**
     * Finds and returns the closest matching enum to a given name. The enum value
     * must contain the {@code find} parameter in it to be valid.
     *
     * @param list list of string values to look through.
     * @param find name of value to find.
     * @return the closest matching string value from the collection.
     */
    public static String findClosestMatch(Collection<String> list, String find, boolean ignoreCase) {
        if (list == null) return null;
        if (ignoreCase) find = find.toLowerCase();
        String closest = null;
        int closestDiff = -1;
        for (String e : list) {
            if (ignoreCase) e = e.toLowerCase();
            if (!e.contains(find)) continue;
            int diff = StringUtil.distance(e, find);
            if (closest == null || diff < closestDiff) {
                closest = e;
                closestDiff = diff;
            }
            if (closestDiff == 0) break;
        }
        return closest;
    }
}
