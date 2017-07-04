package com.astronstudios.natrual.recipes;

import com.astronstudios.natrual.items.RecipeItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

public class SmeltRecipes {

    public SmeltRecipes() {
        FurnaceRecipe recipe = new FurnaceRecipe(new ItemStack(Material.LEATHER), Material.ROTTEN_FLESH);
        Bukkit.getServer().addRecipe(recipe);

        recipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_HELMET), Material.IRON_INGOT);
        Bukkit.getServer().addRecipe(recipe);

        recipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_CHESTPLATE), Material.IRON_INGOT);
        Bukkit.getServer().addRecipe(recipe);

        recipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_LEGGINGS), Material.IRON_INGOT);
        Bukkit.getServer().addRecipe(recipe);

        recipe = new FurnaceRecipe(new ItemStack(Material.CHAINMAIL_BOOTS), Material.IRON_INGOT);
        Bukkit.getServer().addRecipe(recipe);

        recipe = new FurnaceRecipe(RecipeItems.GOOP, Material.LEAVES);
        Bukkit.getServer().addRecipe(recipe);
    }
}
