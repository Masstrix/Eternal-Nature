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

import org.bukkit.ChatColor;

import java.util.List;

public class StringUtil {

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static int distance(String string, String compare) {
        if (compare == null && string != null) return string.length();
        if (string == null && compare != null) return compare.length();
        if (string == null) return 0;
        int diff = 0;
        boolean comparer = compare.length() > string.length();
        int runFor = comparer ? string.length() : compare.length();
        char[] stringArray = string.toCharArray(), compareArray = compare.toCharArray();
        for (int i = 0; i < runFor; i++) {
            if (stringArray[i] != compareArray[i]) diff++;
        }
        return diff;
    }

    public static String fromStringArray(List<String> list, String separator) {
        if (list == null || list.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        for (String s : list) {
            if (builder.length() > 0)
                builder.append(separator);
            builder.append(s);
        }
        return builder.toString();
    }
}
