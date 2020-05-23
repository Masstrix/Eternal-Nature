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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private static final Pattern COLOR_PATTERN = Pattern.compile("(&[a-fk-or0-9])", Pattern.CASE_INSENSITIVE);

    /**
     * Returns the number of occurrences a color code is used in a string. A color
     * code is defined as a & followed by a digit between 0 to 9 or a character from
     * a to f, k to o or r.
     *
     * @param text text to match the string to.
     * @return the number of occurrences a color code is used in the text.
     */
    public static int getColorCodeCount(String text) {
        Matcher matcher = COLOR_PATTERN.matcher(text);
        return (text.length() - matcher.replaceAll("").length()) / 2;
    }

    /**
     * Colorizes a string.
     *
     * @param s string to colorize.
     * @return the same string colorized.
     */
    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Returns the difference between two strings. If both strings are exactly the
     * same 0 will be returned, otherwise the number of differences will be returned.
     *
     * @param string text to check against.
     * @param compare text to compare to.
     * @return the number of differences between {@code string} and {@code compare}.
     */
    public static int distance(String string, String compare) {
        if (compare == null && string != null) return string.length();
        if (string == null && compare != null) return compare.length();
        if (string == null) return 0;
        int diff = Math.abs(string.length() - compare.length());
        boolean comparer = compare.length() > string.length();
        int runFor = comparer ? string.length() : compare.length();
        char[] stringArray = string.toCharArray(), compareArray = compare.toCharArray();
        for (int i = 0; i < runFor; i++) {
            if (stringArray[i] != compareArray[i]) diff++;
        }
        return diff;
    }

    public static int distanceContains(String str, String compare, boolean ignoreCase) {
        if (ignoreCase) {
            str = str.toUpperCase();
            compare = compare.toUpperCase();
        }
        int check = checkDistanceSearch(str, compare);
        if (check != -1) return check;

        if (str.contains(compare)) return str.length() - compare.length();
        return str.length();
    }

    public static int distanceFind(String str, String compare) {
        int check = checkDistanceSearch(str, compare);
        if (check != -1) return check;

        int closest = 0;
        for (int i = 0; i <= str.length() - compare.length(); i++) {
            int match = 0;
            for (int j = 0; j < compare.length(); j++) {
                if (str.charAt(j + i) != compare.charAt(j)) continue;
                match++;
            }
            if (match > 0 && match > closest) {
                closest = match;
            }
        }
        return str.length() + -closest;
    }

    private static int checkDistanceSearch(String str, String compare) {
        if (compare == null && str != null) return str.length();
        if (str == null && compare != null) return compare.length();
        if (str == null) return 0;

        if (compare.length() > str.length()) return 0;
        if (str.equals(compare)) return 0;
        return -1;
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

    public static boolean isInteger(String s) {
        if (s == null) return false;
        boolean negative = s.startsWith("-");
        int lgth = s.length();
        for (int i = negative ? 1 : 0; i < lgth; ++i) {
            if (!isNumber(s.charAt(i))) return false;
        }
        return true;
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }
}
