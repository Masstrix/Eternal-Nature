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

import com.mojang.datafixers.util.Pair;
import me.masstrix.eternalnature.util.ReflectionUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A NMS armor stand. These armor stands are not handled bu the server and will not be
 * visible to players once they leave the server until resent again.
 */
public class ShadowArmorStand extends ShadowEntity {

    private static Class<?> enumItemSlotClass;
    private static Constructor<?> newArmorStand;
    private static Constructor<?> newVector3f;
    private static Constructor<?> newPacketOutEquipment;
    private static Method setMarkerMethod;
    private static Method setInvisibleMethod;
    private static Method setSmallMethod;
    private static Method setHasArmsMethod;
    private static Method setSlotMethod;
    private static Method setHeadPoseMethod;
    private static Method setBodyPoseMethod;
    private static Method setLeftArmPoseMethod;
    private static Method setRightArmPoseMethod;
    private static Method setLeftLegPoseMethod;
    private static Method setRightLegPoseMethod;
    private static Method asNmsItemCopyMethod;

    static {
        try {
            Class<?> armorStandClass = ReflectionUtil.getNmsClass("EntityArmorStand");
            Class<?> vector3fClass = ReflectionUtil.getNmsClass("Vector3f");
            Class<?> itemStackClass = ReflectionUtil.getNmsClass("ItemStack");
            Class<?> craftItemClass = ReflectionUtil.getCraftClass("inventory.CraftItemStack");
            Class<?> packeEquipmentClass = ReflectionUtil.getNmsClass("PacketPlayOutEntityEquipment");
            enumItemSlotClass = ReflectionUtil.getNmsClass("EnumItemSlot");

            newArmorStand = armorStandClass.getConstructor(nmsWorldClass, double.class, double.class, double.class);
            newVector3f = vector3fClass.getConstructor(float.class, float.class, float.class);
            newPacketOutEquipment = packeEquipmentClass.getConstructor(int.class, List.class);

            setMarkerMethod = armorStandClass.getMethod("setMarker", boolean.class);
            setInvisibleMethod = armorStandClass.getMethod("setInvisible", boolean.class);
            setSmallMethod = armorStandClass.getMethod("setSmall", boolean.class);
            setHasArmsMethod = armorStandClass.getMethod("setArms", boolean.class);
            setSlotMethod = armorStandClass.getMethod("setSlot", enumItemSlotClass, itemStackClass);
            asNmsItemCopyMethod = craftItemClass.getMethod("asNMSCopy", ItemStack.class);

            setHeadPoseMethod = armorStandClass.getMethod("setHeadPose", vector3fClass);
            setBodyPoseMethod = armorStandClass.getMethod("setBodyPose", vector3fClass);
            setLeftArmPoseMethod = armorStandClass.getMethod("setLeftArmPose", vector3fClass);
            setRightArmPoseMethod = armorStandClass.getMethod("setRightArmPose", vector3fClass);
            setLeftLegPoseMethod = armorStandClass.getMethod("setLeftLegPose", vector3fClass);
            setRightLegPoseMethod = armorStandClass.getMethod("setRightLegPose", vector3fClass);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private boolean marker;
    private boolean small;
    private boolean hasArms;
    private boolean invisible;
    private final ItemStack[] ITEMS = new ItemStack[ItemSlot.values().length];
    private final Map<ArmorStandBodyPart, Vector> POSE = new WeakHashMap<>();

    /**
     * Creates a new instance of a ShadowArmorStand. These stands are not handled
     * by the server and will not reappear when player rejoins or the server is restarted.
     *
     * @param loc location for the armor stand to be started at.
     */
    public ShadowArmorStand(Location loc) {
        super(loc);
        Object world = asNmsWorld(loc.getWorld());

        try {
            entity = newArmorStand.newInstance(world, loc.getX(), loc.getY(), loc.getZ());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return if the armor stand is set as a marker.
     */
    public boolean isMarker() {
        return marker;
    }

    /**
     * Sets if the armor stand will be a marker. When an armor stand is a marker it's hitbox
     * is set to be almost nothing meaning the name tag will be right where the stands position is,
     * having not offset (for example if the armor stands location was on the ground and set as a
     * marker, the name tag would display on the ground instead of a couple blocks up). This also
     * means it will not be in the way of players interactions (hits, clicks etc).
     *
     * @param marker if true the armor stand will be set to act as a marker.
     */
    public void setMarker(boolean marker) {
        try {
            setMarkerMethod.invoke(super.entity, marker);
            sendRefresh();
            this.marker = marker;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return if the armor stand is visible
     */
    public boolean isInvisible() {
        return invisible;
    }

    /**
     * Sets if the armor stand is invisible.
     *
     * @param invisible if true the armor stand will be hidden.
     */
    public void setInvisible(boolean invisible) {
        try {
            setInvisibleMethod.invoke(entity, invisible);
            sendRefresh();
            this.invisible = invisible;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return if the armor stand is currently small.
     */
    public boolean isSmall() {
        return small;
    }

    /**
     * Sets if the armor stand is small.
     *
     * @param small if true the armor stand will be small, otherwise it will
     *              be it's normal full size.
     */
    public void setSmall(boolean small) {
        try {
            setSmallMethod.invoke(entity, small);
            sendRefresh();
            this.small = small;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return if the armor stand has arms.
     */
    public boolean hasArms() {
        return hasArms;
    }

    /**
     * Sets if the armor stands arms are visible.
     *
     * @param hasArms if true the armor stands arms will be visible.
     */
    public void setArms(boolean hasArms) {
        try {
            setHasArmsMethod.invoke(entity, hasArms);
            sendRefresh();
            this.hasArms = hasArms;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the item in a slot.
     *
     * @param slot slot to place the item in.
     * @param item item being placed in the slot.
     */
    public void setSlot(ItemSlot slot, ItemStack item) {
        try {
            // Set the items
            Object[] constants =  enumItemSlotClass.getEnumConstants();
            Object nmsItem = asNmsItemCopyMethod.invoke(asNmsItemCopyMethod, item);
            setSlotMethod.invoke(entity, constants[slot.getSlot()], nmsItem);
            ITEMS[slot.getSlot()] = item;

            // Create a list of items from the array
            List<Pair<Object, Object>> itemList = new ArrayList<>();
            itemList.add(new Pair<>(constants[slot.getSlot()], nmsItem));

            // Create and send the update equipment packet
            Object packet = newPacketOutEquipment.newInstance(getEntityId(), itemList);
            sendPacket(packet);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the item currently equipped in a slot.
     *
     * @param slot slot to get the item for.
     * @return the item currently in that slot or null if the slot is empty.
     */
    public ItemStack getEquipment(ItemSlot slot) {
        return ITEMS[slot.getSlot()];
    }

    /**
     * Sets the pose of a certain body part of the armor stand.
     *
     * @param part part of the body to set the pose for,
     * @param vec  vector to apply for the body parts rotation. The vector is
     *             in the format of pitch, roll and yaw.
     */
    public void setPose(ArmorStandBodyPart part, Vector vec) {
        float x = (float) vec.getX(); // Pitch
        float y = (float) vec.getY(); // Roll
        float z = (float) vec.getZ(); // Yaw
        try {
            Object asVector3f = newVector3f.newInstance(x, y, z);

            switch (part) {
                case HEAD: {
                    setHeadPoseMethod.invoke(entity, asVector3f);
                    break;
                }
                case BODY: {
                    setBodyPoseMethod.invoke(entity, asVector3f);
                    break;
                }
                case LEFT_ARM: {
                    setLeftArmPoseMethod.invoke(entity, asVector3f);
                    break;
                }
                case RIGHT_ARM: {
                    setRightArmPoseMethod.invoke(entity, asVector3f);
                    break;
                }
                case LEFT_LEG: {
                    setLeftLegPoseMethod.invoke(entity, asVector3f);
                    break;
                }
                case RIGHT_LEG: {
                    setRightLegPoseMethod.invoke(entity, asVector3f);
                    break;
                }
            }
            POSE.put(part, vec);
            sendRefresh();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the pose for a part of the armor stand.
     *
     * @param part part of the armor stand to get the current pose for,
     * @return the armor pose for the part.
     */
    public Vector getPose(ArmorStandBodyPart part) {
        return POSE.getOrDefault(part, new Vector());
    }

    @Override
    public void sendTo(Player player) {
        super.sendTo(player);

        // Create and send the update equipment packet
        try {
            // Create a list of items from the array
            List<Pair<Object, Object>> itemList = new ArrayList<>();
            for (ItemSlot slot : ItemSlot.values()) {
                Object nmsItem = asNmsItemCopyMethod.invoke(asNmsItemCopyMethod, ITEMS[slot.getSlot()]);
                itemList.add(new Pair<>(slot.asNMS(), nmsItem));
            }

            Object packet = newPacketOutEquipment.newInstance(getEntityId(), itemList);
            sendPacket(packet);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
