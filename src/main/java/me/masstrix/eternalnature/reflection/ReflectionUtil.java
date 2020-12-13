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

package me.masstrix.eternalnature.reflection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtil {

    private static String version = null;
    private static byte[] versionUnsafe = null;

    private static Class<?> packetClass; // Cache packet class.
    private static Class<?> craftPlayer; // Cache CraftPlayer class.
    private static Method getHandlePlayerMethod;

    static {
        try {
            packetClass = getNmsClass("Packet");
            craftPlayer = getCraftClass("entity.CraftPlayer");
            getHandlePlayerMethod = craftPlayer.getMethod("getHandle");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the servers version as a safe string.
     */
    public static String getVersion() {
        if (version == null || version.length() == 0) {
            String ver = Bukkit.getServer().getClass().getPackage().getName();
            version = ver.substring(ver.lastIndexOf(".") + 1);
        }
        return version;
    }

    /**
     * @return the servers version in a unsafe method.
     */
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

    /**
     * Returns a class from the base server directory.
     *
     * @param name name of the class.
     * @return the class.
     * @throws ClassNotFoundException
     */
    public static Class<?> getNmsClass(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getVersion() + "." + name);
    }

    /**
     * Returns a class from the craft bukkit section of the server.
     *
     * @param name name of the class.
     * @return the class.
     * @throws ClassNotFoundException
     */
    public static Class<?> getCraftClass(String name) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
    }

    /**
     * Get a players connection that is currently online. This connection
     * can be used to send the packets to the player.
     *
     * @param player who's connection you are getting.
     * @return the players connection.
     */
    public static Object getConnection(Player player) {
        try {
            Object craft = craftPlayer.cast(player);
            Object handle = getHandlePlayerMethod.invoke(craft);
            return handle.getClass().getField("playerConnection").get(handle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendPacket(Player to, Object... packet) {
        if (to == null || packet == null || packet.length == 0) return;
        Object pc = getConnection(to);
        if (pc == null) return;
        try {
            Method sendMethod = pc.getClass().getDeclaredMethod("sendPacket", packetClass);
            for (Object p : packet)
                sendMethod.invoke(pc, p);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
