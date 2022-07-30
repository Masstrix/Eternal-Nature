package me.masstrix.eternalnature.core.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class WaterBottleItem extends EternalItem {

    WaterBottleItem() {
        super(Material.GLASS_BOTTLE);
    }

    /**
     * Gets an item stack and creates a water bottle item from it.
     *
     * @param item item stack to use.
     */
    @Override
    public void fromItem(ItemStack item) {
        super.fromItem(item);
    }
}
