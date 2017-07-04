package com.astronstudios.natrual.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class ReflectionUtil {

    public static String getVersion() {
        String ver = Bukkit.getServer().getClass().getPackage().getName();
        return ver.substring(ver.lastIndexOf(".") + 1);
    }

    public static String getNMSPlath() {
        return "net.minecraft.server." + getVersion();
    }

    public static String getOBCPlath() {
        return "org.bukkit.craftbukkit." + getVersion();
    }

    public static Class<?> getNMSClass(String plath) {
        Class clazz = null;
        String c = getNMSPlath() + "." + plath;

        try {
            clazz = Class.forName(c);
        } catch (ClassNotFoundException var4) {
            var4.printStackTrace();
        }

        return clazz;
    }

    public static Class<?> getOBCClass(String plath) {
        Class clazz = null;
        String c = getOBCPlath() + "." + plath;

        try {
            clazz = Class.forName(c);
        } catch (ClassNotFoundException var4) {
            var4.printStackTrace();
        }

        return clazz;
    }

    public static Object getConnection(Player player) {
        try {
            Object e = player.getClass().getMethod("getHandle", new Class[0]).invoke(player, 0);
            return e.getClass().getField("playerConnection").get(e);
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static void sendPacket(Player player, Object packet) {
        if (player != null && packet != null) {
            try {
                Object e = getConnection(player);
                if (e != null) {
                    Method m = e.getClass().getDeclaredMethod("sendPacket", getNMSClass("Packet"));
                    m.invoke(e, packet);
                } else {
                    System.out.print("No playerConnection found for " + player.getName());
                }
            } catch (Exception var4) {
                var4.printStackTrace();
            }

        }
    }
}
