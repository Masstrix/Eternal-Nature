package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.NaturalEnvironment;
import com.astronstudios.natrual.items.RecipeItems;
import com.astronstudios.natrual.util.CustomStack;
import com.astronstudios.natrual.recipes.CraftRecipes;
import net.minecraft.server.v1_11_R1.NBTTagInt;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class InventoryListener implements Listener {

    private static List<String> tags = new ArrayList<>();

    static {
        tags.add("leaveball");
        tags.add("campfire");
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.FURNACE) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getClickedInventory().getItem(0) != null && event.getClickedInventory().getItem(0).getType() == Material.LEAVES) {
                        ItemStack stack = event.getClickedInventory().getItem(0);
                        Inventory inv = event.getClickedInventory();
                        Object o1 = CustomStack.getNBTTag(stack, "leaveball");
                        if (o1 == null) {
                            inv.setItem(0, new ItemStack(Material.AIR));
                            event.setCurrentItem(stack);
                        }
                    }
                }
            }.runTaskLater(NaturalEnvironment.getInstance(), 1);
        }
    }

    @EventHandler
    public void on(InventoryMoveItemEvent event) {
        if (event.getDestination().getType() == InventoryType.FURNACE) {
            if (event.getItem() != null) {
                ItemStack stack = event.getItem();
                for (String s : tags) {
                    Object o1 = CustomStack.getNBTTag(stack, s);
                    if (o1 == null) event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(PrepareItemCraftEvent e) {
        if (e.getInventory() != null) {
            CraftingInventory inv = e.getInventory();

            if (e.getRecipe().equals(new CraftRecipes().backpack())) {
                ItemStack item1 = inv.getMatrix()[0];
                ItemStack item2 = inv.getMatrix()[2];

                boolean t = true;

                Object o1, o2;
                o1 = CustomStack.getNBTTag(item1, "backpack");
                o2 = CustomStack.getNBTTag(item2, "backpack");

                if (o1 == null || o2 == null) t = false;
                if (o1 != "strap" || o2 != "strap") t = false;

                if (!t) {
                    inv.setResult(new ItemStack(Material.AIR));
                }
            }
            else if (e.getRecipe().getResult().getType() == Material.SLIME_BALL) {
                System.out.print("!");
                for (ItemStack item : inv.getMatrix()) {
                    System.out.print(item);
                    if (item != null && item.getType() != Material.AIR
                            && CustomStack.getNBTTag(item, "goop") == null) {
                        inv.setResult(new ItemStack(Material.AIR));
                        break;
                    }
                }
            }
            else if (e.getRecipe().getResult().isSimilar(RecipeItems.HYDRATION_CHEST_1)) {
                ItemStack item1 = inv.getMatrix()[4];

                boolean t = true;

                Object o1;
                o1 = CustomStack.getNBTTag(item1, "hydration.pump");

                if (o1 == null) t = false;

                if (!t) {
                    inv.setResult(new ItemStack(Material.AIR));
                }
            }
            else if (e.getRecipe().getResult().isSimilar(RecipeItems.HYDRATION_CHEST_2)) {
                ItemStack item1 = inv.getMatrix()[4];

                boolean t = true;

                Object o1;
                o1 = CustomStack.getNBTTag(item1, "hydration.level");

                if (o1 == null || ((NBTTagInt) o1).e() != 1) t = false;

                if (!t) {
                    inv.setResult(new ItemStack(Material.AIR));
                }
            }
            else if (e.getRecipe().getResult().isSimilar(RecipeItems.HYDRATION_CHEST_3)) {
                ItemStack item1 = inv.getMatrix()[4];

                boolean t = true;

                Object o1;
                o1 = CustomStack.getNBTTag(item1, "hydration.level");

                if (o1 == null || ((NBTTagInt) o1).e() != 2) t = false;

                if (!t) {
                    inv.setResult(new ItemStack(Material.AIR));
                }
            }
        }
    }
}
