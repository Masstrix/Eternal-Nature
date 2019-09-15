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
