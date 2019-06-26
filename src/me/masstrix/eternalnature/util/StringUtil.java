package me.masstrix.eternalnature.util;

import org.bukkit.ChatColor;

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
}
