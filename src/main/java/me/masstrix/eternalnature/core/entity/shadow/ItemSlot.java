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

//import net.minecraft.world.entity.EnumItemSlot;

import net.minecraft.world.entity.EquipmentSlot;

public enum ItemSlot {
    MAINHAND,
    OFFHAND,
    FEET,
    LEGS,
    CHEST,
    HEAD;

    private final EquipmentSlot NMS;

    ItemSlot() {
        this.NMS = EquipmentSlot.values()[this.ordinal()];
    }

    public boolean isArmorSlot() {
        return this.ordinal() > OFFHAND.ordinal();
    }

    public EquipmentSlot nms() {
        return NMS;
    }
}
