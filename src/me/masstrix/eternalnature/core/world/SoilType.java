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

public enum SoilType {

    GROUND(Material.GRASS_BLOCK, Material.DIRT, Material.PODZOL),
    FARM(Material.FARMLAND);

    private final Material[] blocks;

    SoilType(Material... blocks) {
        this.blocks = blocks;
    }

    /**
     * Returns if a material is apart of the soil type group.
     *
     * @param material type to check if it matches the soil.
     * @return if the material is apart of the soil group.
     */
    public boolean isValidBlock(Material material) {
        for (Material m : blocks) {
            if (m == material) return true;
        }
        return false;
    }
}
