/*
 * Copyright 2019 Matthew Denton
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

package me.masstrix.eternalnature.core.world;

import org.bukkit.Material;

public enum TreeProduct {

    LOG, BARK, PLANKS, SAPLING, BUTTON, DOOR, FENCE, LEAVES, SIGN, SLAB;

    private static final String syntax = "%s%_%s%";

    /**
     * Converts a tree product to a different product. For example a oak_door to
     * an oak_sapling.
     *
     * @param material material to convert.
     * @return
     */
    public Material convert(Material material) {
        String[] components = material.name().split("_");

        TreeType type = TreeType.find(components[0]);
        if (type != null) {
            return Material.valueOf(type.name() + "_" + this.name());
        }
        return material;
    }

    private enum TreeType {
        OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK;

        static TreeType find(String name) {
            for (TreeType t : values()) {
                if (t.name().equalsIgnoreCase(name))
                    return t;
            }
            return null;
        }
    }
}
