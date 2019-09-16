package me.masstrix.eternalnature.core.world;

import org.bukkit.Material;
import org.bukkit.block.data.type.Sapling;

public enum PlantType {

    FLOWER(SoilType.GROUND),
    CROP(SoilType.FARM),
    SAPLING(SoilType.GROUND);

    private final SoilType soilType;

    PlantType(SoilType soilType) {
        this.soilType = soilType;
    }

    /**
     * Returns the required {@link SoilType} for the plant.
     *
     * @return the required soil type.
     */
    public SoilType getSoilType() {
        return soilType;
    }

    /**
     * Returns the plant type of a material or null if it is not a plant.
     *
     * @param type material to check.
     * @return the plant type or null if not a plant.
     */
    public static PlantType fromMaterial(Material type) {
        if (isCrop(type))
            return CROP;
        if (isFlower(type))
            return FLOWER;
        if (isSapling(type))
            return SAPLING;
        return null;
    }

    /**
     * Returns if an Item entity is a sapling.
     *
     * @param type item to check if is a sapling.
     * @return true if the item is a sapling.
     */
    public static boolean isSapling(Material type) {
        return type.isBlock() && type.createBlockData() instanceof Sapling;
    }

    /**
     * Returns if a material is a valid plant type.
     *
     * @param type material to check if it's a plant.
     * @return true if is a valid plant.
     */
    public static boolean isPlant(Material type) {
        return isCrop(type) || isFlower(type) || isSapling(type);
    }

    /**
     * Returns if a material is a flower.
     *
     * @param type material to check.
     * @return true if material is a flower.
     */
    public static boolean isFlower(Material type) {
        switch (type) {
            case POPPY:
            case DANDELION:
            case BLUE_ORCHID:
            case ALLIUM:
            case AZURE_BLUET:
            case RED_TULIP:
            case ORANGE_TULIP:
            case WHITE_TULIP:
            case PINK_TULIP:
            case OXEYE_DAISY:
            case CORNFLOWER:
            case LILY_OF_THE_VALLEY:
            case WITHER_ROSE:
            case SUNFLOWER:
            case LILAC:
            case ROSE_BUSH:
            case PEONY:
            case FERN:
            case GRASS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns if a a material can be replanted automatically if
     * auto replant is enabled in the config.
     *
     * @param type material to check.
     * @return if the crop can be auto replanted.
     */
    public static boolean isReplantableCrop(Material type) {
        switch (type) {
            case POTATOES:
            case POTATO:
            case CARROTS:
            case CARROT:
            case BEETROOTS:
            case BEETROOT_SEEDS:
            case WHEAT_SEEDS:
            case WHEAT:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns if a material is a seed for a crop.
     *
     * @param type material to check if it is a seed.
     * @return if a material is a seed for crops.
     */
    public static boolean isCropSeed(Material type) {
        switch (type) {
            case POTATO:
            case CARROT:
            case BEETROOT_SEEDS:
            case WHEAT_SEEDS:
            case PUMPKIN_SEEDS:
            case MELON_SEEDS:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns if a material is a farming item. Farming items require the soil
     * to be <i>Farmland</i>. Such as seeds, and beetroot.
     *
     * @param type material type.
     * @return true if farming item.
     */
    public static boolean isCrop(Material type) {
        switch (type) {
            case POTATOES:
            case POTATO:
            case CARROTS:
            case CARROT:
            case BEETROOTS:
            case BEETROOT_SEEDS:
            case WHEAT_SEEDS:
            case WHEAT:
            case PUMPKIN_SEEDS:
            case MELON_SEEDS:
            case SWEET_BERRIES:
                return true;
            default:
                return false;
        }
    }
}
