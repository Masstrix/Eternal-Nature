package me.masstrix.eternalnature.util;

import org.bukkit.Bukkit;

public class ReflectionUtil {

    private static String version = null;
    private static byte[] versionUnsafe = null;

    public static String getVersion() {
        if (version == null || version.length() == 0) {
            String ver = Bukkit.getServer().getClass().getPackage().getName();
            version = ver.substring(ver.lastIndexOf(".") + 1);
        }
        return version;
    }

    public static byte[] getVersionUnsafe() {
        if (versionUnsafe == null) {
            String version = Bukkit.getVersion();
            String data = version.split("\\(MC: ")[1];
            data = data.substring(0, data.length() - 1);
            String[] cut = data.split("\\.");
            versionUnsafe = new byte[cut.length];
            for (int i = 0; i < cut.length; i++) {
                versionUnsafe[i] = Byte.parseByte(cut[i]);
            }
        }
        return versionUnsafe;
    }
}
