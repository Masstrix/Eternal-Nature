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

import me.masstrix.eternalnature.reflection.ReflectionUtil;

public enum ItemSlot {
    MAINHAND(0, 0),
    OFFHAND(1, 1),
    FEET(2, 0),
    LEGS(3, 1),
    CHEST(4, 2),
    HEAD(5, 3);

    private static Class<?> enumItemSlotClass;

    static {
        try {
            enumItemSlotClass = ReflectionUtil.getNmsClass("EnumItemSlot");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int slot;
    private int ordinal;
    private Object nms;

    ItemSlot(int slot, int ordinal) {
        this.slot = slot;
        this.ordinal = ordinal;
    }

    public int getSlot() {
        return slot;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public boolean isArmorSlot() {
        return slot > OFFHAND.slot;
    }

    /**
     * @return the slot as the nms equivalent.
     */
    public Object asNMS() {
        if (nms == null) {
            nms = enumItemSlotClass.getEnumConstants()[slot];
        }
        return nms;
    }
}
