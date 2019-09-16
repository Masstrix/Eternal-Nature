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
