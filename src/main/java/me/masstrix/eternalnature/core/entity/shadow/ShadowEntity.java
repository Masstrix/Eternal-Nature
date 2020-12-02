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

package me.masstrix.eternalnature.core.entity.shadow;

import me.masstrix.eternalnature.util.ReflectionUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public abstract class ShadowEntity {

    protected static Class<?> nmsWorldClass;
    private static Class<?> entityClass;
    private static Class<?> chatComponentClass;
    private static Class<?> craftWorldClass;
    private static Constructor<?> newSpawnEntity;
    private static Constructor<?> newOutEntityMetaData;
    private static Constructor<?> teleportPacketConstructor;
    private static Constructor<?> destroyPacketConstructor;
    private static Constructor<?> packetRelEntityMove;
    private static Method getCraftWorldHandle;
    private static Method setPositionMethod;
    private static Method setSilentMethod;
    private static Method setNoGravityMethod;

    static {
        try {
            // Classes
            Class<?> spawnEntityPacketClass = ReflectionUtil.getNmsClass("PacketPlayOutSpawnEntity");
            Class<?> teleportPacketClass = ReflectionUtil.getNmsClass("PacketPlayOutEntityTeleport");
            Class<?> destroyPacketClass = ReflectionUtil.getNmsClass("PacketPlayOutEntityDestroy");
            Class<?> relativeMove = ReflectionUtil.getNmsClass("PacketPlayOutEntity$PacketPlayOutRelEntityMove");
            Class<?> packetPlayOutEntityMetadata = ReflectionUtil.getNmsClass("PacketPlayOutEntityMetadata");
            Class<?> dataWatcherClass = ReflectionUtil.getNmsClass("DataWatcher");
            entityClass = ReflectionUtil.getNmsClass("Entity");
            chatComponentClass = ReflectionUtil.getNmsClass("IChatBaseComponent");
            nmsWorldClass = ReflectionUtil.getNmsClass("World");
            craftWorldClass = ReflectionUtil.getCraftClass("CraftWorld");

            // Constructors
            newSpawnEntity = spawnEntityPacketClass.getConstructor(entityClass);
            newOutEntityMetaData = packetPlayOutEntityMetadata.getConstructor(int.class, dataWatcherClass, boolean.class);
            teleportPacketConstructor = teleportPacketClass.getConstructor(entityClass);
            destroyPacketConstructor = destroyPacketClass.getConstructor(int[].class);
            packetRelEntityMove = relativeMove.getConstructor(int.class, short.class, short.class, short.class, boolean.class);

            // Methods
            getCraftWorldHandle = craftWorldClass.getDeclaredMethod("getHandle");
            setPositionMethod = entityClass.getMethod("setPositionRotation",
                    double.class, double.class, double.class, float.class, float.class);
            setSilentMethod = entityClass.getMethod("setSilent", boolean.class);
            setNoGravityMethod = entityClass.getMethod("setNoGravity", boolean.class);

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    protected Object asCraftWorld(World world) {
        return craftWorldClass.cast(world);
    }

    protected Object asNmsWorld(World world) {
        Object craftWorld = asCraftWorld(world);
        try {
            return getCraftWorldHandle.invoke(craftWorld);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final Set<Player> VISIBLE_TO = Collections.newSetFromMap(new WeakHashMap<>());
    private int id = -1;
    protected Object entity;
    protected Object dataWatcher;
    private Location location;

    /**
     * Creates a new instance of a shadow entity.
     *
     * @param loc origin location for the entity to be.
     */
    public ShadowEntity(Location loc) {
        Validate.notNull(loc);
        this.location = loc;
        new ShadowEntityManager().register(this);
    }

    /**
     * Returns the entities id.
     *
     * @return returns the entities id. If the entity has not been created yet then
     *         it will return -1.
     */
    public int getEntityId() {
        if (id == -1 && entity != null) {
            try {
                Field idField = entityClass.getDeclaredField("id");
                idField.setAccessible(true);
                this.id = idField.getInt(entity);
                idField.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public void setSilent(boolean silent) {
        try {
            setSilentMethod.invoke(entity, silent);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setYaw(float yaw) {
        setYawPitch(yaw, location.getPitch());
    }

    public void setPitch(float pitch) {
        setYawPitch(location.getYaw(), pitch);
    }

    /**
     * Rotates the entities yaw and pitch directions adding to the current
     * yaw and pitch.
     *
     * @param yaw yaw in degrees.
     * @param pitch pitch in degrees.
     */
    public void rotate(float yaw, float pitch) {
        if (yaw == 0 && pitch == 0) return;
        setYawPitch(location.getYaw() + yaw, location.getPitch() + pitch);
    }

    /**
     * Sets the yaw and pitch of the entities location.
     *
     * @param yaw yaw in degrees.
     * @param pitch pitch in degrees.
     */
    public void setYawPitch(float yaw, float pitch) {
        try {
            Method setPos = entityClass.getMethod("setYawPitch", float.class, float.class);
            setPos.setAccessible(true);
            setPos.invoke(entity, yaw, pitch);
            setPos.setAccessible(false);
            Object packet = teleportPacketConstructor.newInstance(entity);
            sendPacket(packet);

            location.setYaw(yaw);
            location.setPitch(pitch);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the current location of the entity.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location of the entity and sends the updates required to move it to that location.
     * This will act as both a move ot teleport depending on how far the entity moves. If the entity
     * moves more than 8 blocks then it will teleport otherwise it will move with higher precision.
     *
     * @param loc location to move the entity to,
     */
    public void setLocation(Location loc) {
        try {
            double x = this.location.getX() + loc.getX();
            double y = this.location.getY() + loc.getY();
            double z = this.location.getZ() + loc.getZ();

            // Update position in nms object
            setPositionMethod.invoke(entity, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

            if (x < 8 && y < 8 && z < 8) {
                // Move Rel
                short moveX = (short) ((x * 32 - this.location.getX() * 32) * 128);
                short moveY = (short) ((x * 32 - this.location.getY() * 32) * 128);
                short moveZ = (short) ((x * 32 - this.location.getZ() * 32) * 128);
                Object packet = packetRelEntityMove.newInstance(getEntityId(), moveX, moveY, moveZ, false);
                sendPacket(packet);
            } else {
                // Teleport
                Object teleport = teleportPacketConstructor.newInstance(entity);
                sendPacket(teleport);
            }

            this.location = loc;
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Moves the entity in a direction relative to where it currently is.
     *
     * @param x x direction to move in.
     * @param y y direction to move in.
     * @param z z direction to move in.
     */
    public void move(double x, double y, double z) {
        setLocation(this.location.clone().add(x, y, z));
    }

    /**
     * Moves the entity in a direction relative to where it currently is.
     *
     * @param vector vector to add to the entities current location.
     */
    public void move(Vector vector) {
        move(vector.getX(), vector.getY(), vector.getZ());
    }

    /**
     * Removes the entity for all players who can currently see it.
     */
    public void remove() {
        VISIBLE_TO.forEach(this::hideFrom);
    }

    /**
     * Sends the packets to make the entity visible to the player and adds them to the list of
     * current viewers of the entity for when it is removed.
     *
     * @param player who this entity is being sent to.
     */
    public void sendTo(Player player) {
        if (entity == null) return;
        getEntityId(); // Make sure the entity ID is set.
        try {
            Object spawnPacket = newSpawnEntity.newInstance(entity);
            ReflectionUtil.sendPacket(player, spawnPacket, getRefreshPacket());
            VISIBLE_TO.add(player);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Hides the entity from a specific player. If the entity was never sent to them
     * then this will do nothing.
     *
     * @param player who this entity is being hidden from.
     */
    public void hideFrom(Player player) {
        if (entity == null || VISIBLE_TO.size() == 0) return;
        try {
            //noinspection PrimitiveArrayArgumentToVarargsMethod
            Object packet = destroyPacketConstructor.newInstance(new int[] {id});
            ReflectionUtil.sendPacket(player, packet);
            VISIBLE_TO.remove(player);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a refresh packet for the entity to all players who can currently see
     * the entity.
     */
    public void sendRefresh() {
        if (VISIBLE_TO.size() > 0) sendPacket(getRefreshPacket());
    }
    /**
     * @return the datawatcher for the entity object.
     */
    protected Object getDataWatcher() {
        if (dataWatcher == null) {
            try {
                dataWatcher = entityClass.getMethod("getDataWatcher").invoke(this.entity);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return dataWatcher;
    }

    /**
     * Returns the packet used to send a refresh update for the entity. This needs to be sent when
     * some part of the entity's meta data is changed and needs updating on the clients end.
     *
     * @return the packet used to refresh the entities metadata.
     */
    private Object getRefreshPacket() {
        try {
            return newOutEntityMetaData.newInstance(getEntityId(), getDataWatcher(), false);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Sends a packet to all players who can currently see the entity.
     *
     * @param packet packet(s) that are being sent to the players.
     */
    protected final void sendPacket(Object... packet) {
        VISIBLE_TO.forEach(player -> ReflectionUtil.sendPacket(player, packet));
    }
}
