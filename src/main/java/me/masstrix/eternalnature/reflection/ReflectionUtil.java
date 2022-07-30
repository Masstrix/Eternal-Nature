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

import me.masstrix.eternalnature.packet.WrappedPacket;
//import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
//import net.minecraft.server.level.EntityPlayer;
//import net.minecraft.server.level.WorldServer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {

    // Version meta
    private static String version = null;
    private static byte[] versionUnsafe = null;

    // Class cache
    private static Class<?> packetClass;
    private static Class<?> craftPlayer;
    private static Class<?> craftWorld;
    private static Class<?> craftItem;

    // Method cache
    private static Method getHandlePlayerMethod;
    private static Method getWorldHandleMethod;
    private static Method asNMSItemMethod;

    // Field cache
    private static String playerConnectionField;

    // Setup all the cache
    static {
        try {
            packetClass = getMcProtocol("Packet");
            craftPlayer = getCraftClass("entity.CraftPlayer");
            craftWorld = getCraftClass("CraftWorld");
            // Cache CraftPlayer class.
            craftItem = getCraftClass("inventory.CraftItemStack");

            getHandlePlayerMethod = craftPlayer.getMethod("getHandle");
            getWorldHandleMethod = craftWorld.getDeclaredMethod("getHandle");
            asNMSItemMethod = craftItem.getMethod("asNMSCopy", ItemStack.class);

            // Locate the name of the PlayerConnection field in the EntityPlayer class
            for (Field f : net.minecraft.world.entity.player.Player.class.getDeclaredFields()) {
                if (f.getType().getSimpleName().equals("PlayerConnection")) {
                    playerConnectionField = f.getName();
                    break;
                }
            }

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
     * Returns the WorldServer of a world. This converts it into a minecraft instance
     * bypassing the CraftWorld using non-version dependent classes.
     *
     * @param world world to get the world server for.
     * @return the world's WorldServer
     */
//    public static WorldServer getWorldHandle(World world) {
//        Object cWorld = craftWorld.cast(world);
//        try {
//            return (WorldServer) getWorldHandleMethod.invoke(cWorld);
//        } catch (IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * Returns a converted bukkit ItemStack as a minecraft ItemStack.
     *
     * @param item item to convert.
     * @return the converted ItemStack.
     */
    public static net.minecraft.world.item.ItemStack asNmsItem(ItemStack item) {
        try {
            return (net.minecraft.world.item.ItemStack) asNMSItemMethod.invoke(craftItem, item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
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

    public static Class<?> getMcProtocol(String name) throws ClassNotFoundException {
        return Class.forName("net.minecraft.network.protocol." + name);
    }

    public static Class<?> getClass(String path) throws ClassNotFoundException {
        return Class.forName(path);
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
            return handle.getClass().getField(playerConnectionField).get(handle);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a packet to a player. This accepts any class that extends the minecraft {@code Packet}
     * class or a {@link WrappedPacket}.
     *
     * @param to        who this packet is being sent to.
     * @param packet    the packet or list of packets being sent to the player.
     */
    public static void sendPacket(Player to, Object... packet) {
        if (to == null || packet == null || packet.length == 0) return;
//        Object pc = getConnection(to);
        CraftPlayer craftplayer = (CraftPlayer) to;
        ServerGamePacketListenerImpl connection = craftplayer.getHandle().connection;
//        if (pc == null) return;
        //            Method sendMethod = pc.getClass().getDeclaredMethod("sendPacket", packetClass);
        for (Object p : packet) {
//                TODO re add wrapping
//                if (p instanceof WrappedPacket)
//                    p = ((WrappedPacket) p).getPacket();
            connection.send((Packet<?>) p);
//                sendMethod.invoke(pc, p);
        }
    }
}
