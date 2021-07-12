package me.masstrix.eternalnature.core.item;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class LeafItem extends EternalItem {

    private static final Map<String, ItemStack> ITEMS = new HashMap<>();

    static {
        // Store a tmp list of all leaf types.
        Map<String, Integer> index = new HashMap<>();
        index.put("oak", 1);
        index.put("spruce", 2);
        index.put("birch", 3);
        index.put("jungle", 4);
        index.put("acacia", 5);

        // Create a leaf variant for each leaf color.
        for (LeafColor b : LeafColor.values()) {
            index.forEach((key, value) -> {
                ItemBuilder item = new ItemBuilder(Material.KELP);
                item.setName("&f" + key + " leaf");
                item.setCustomModelData(EternalItem.MODEL_BASE + value + b.ID);
                ITEMS.put(key + "_" + b.simple(), item.build());
            });
        }
    }

    private String type = "unknown";
    private LeafColor color = LeafColor.NORMAL;
    private ItemStack stack;

    /**
     * A simple enum to store each color of leaf. This allows for a different color
     * variant for each biome's color.
     */
    public enum LeafColor {
        NORMAL(0, "forest"),
        HUMID(100, "jungle");
        //VERY_DRY(200, "desert", "savanna"),
        //DRY(300, "badlands");

        private final int ID;
        private final String[] TYPE_MATCH;

        LeafColor(int id, String... biomeTypeMatch) {
            this.ID = id;
            this.TYPE_MATCH = biomeTypeMatch;
        }

        public String simple() {
            return name().toLowerCase();
        }

        /**
         * @param biome biome to check if it matches.
         * @return if the biome provided matches this leaf biome style.
         */
        public boolean matches(Biome biome) {
            for (String s : TYPE_MATCH) {
                if (biome.name().toLowerCase().contains(s.toLowerCase()))
                    return true;
            }
            return false;
        }

        /**
         * @param biome biome to get the leaf color for.
         * @return the color that matches the provided biome.
         */
        public static LeafColor fromBiome(Biome biome) {
            for (LeafColor color : LeafColor.values()) {
                if (color.matches(biome)) return color;
            }
            return NORMAL;
        }
    }

    public LeafItem() {
        super(Material.KELP);
        stack = super.create();
    }

    /**
     * Sets the color of the leaf. This is used to set the variant of the leaf
     * between biomes matching it better with each biome's different colors.
     *
     * @param color color to set the leaf to use.
     * @return an instance of this item.
     */
    public LeafItem color(LeafColor color) {
        this.color = color == null ? this.color : color;
        return this;
    }

    /**
     * Sets the color of the leaf. This is used to set the variant of the leaf
     * between biomes matching it better with each biome's different colors.
     *
     * @param biome biome to match the color to.
     * @return an instance of this item.
     */
    public LeafItem color(Biome biome) {
        this.color = LeafColor.fromBiome(biome);
        return this;
    }

    /**
     * Returns the corresponding leaf item from a material type. By default
     * this will return the oak leaf if it is not known.
     *
     * @param type material to get the leave type from.
     * @return the leaf item.
     */
    public LeafItem fromMaterial(Material type) {
        switch (type) {
            case ACACIA_LEAVES -> stack = this.acacia();
            case BIRCH_LEAVES -> stack = this.birch();
            case SPRUCE_LEAVES -> stack = this.spruce();
            case JUNGLE_LEAVES -> stack = this.jungle();
            default -> stack = this.oak();
        }
        return this;
    }

    public ItemStack oak() {
        type = "oak";
        return ITEMS.get("oak_" + color.simple()).clone();
    }

    public ItemStack spruce() {
        type = "spruce";
        return ITEMS.get("spruce_" + color.simple()).clone();
    }

    public ItemStack birch() {
        type = "birch";
        return ITEMS.get("birch_" + color.simple()).clone();
    }

    public ItemStack jungle() {
        type = "jungle";
        return ITEMS.get("jungle_" + color.simple()).clone();
    }

    public ItemStack acacia() {
        type = "acacia";
        return ITEMS.get("acacia_" + color.simple()).clone();
    }

    public LeafColor getColor() {
        return color;
    }

    public String getType() {
        return type;
    }

    @Override
    public ItemStack create() {
        return stack;
    }
}
