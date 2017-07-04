package com.astronstudios.natrual.recipes;

import com.astronstudios.natrual.items.Backpack;
import com.astronstudios.natrual.items.RecipeItems;
import com.astronstudios.natrual.util.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class CraftRecipes {

    public void register() {
        new RecipeItems().load();
        Bukkit.getServer().addRecipe(nametag());
        Bukkit.getServer().addRecipe(leafBall());
        Bukkit.getServer().addRecipe(slimeBall());
        Bukkit.getServer().addRecipe(campfire());
        Bukkit.getServer().addRecipe(advancedClock());
        Bukkit.getServer().addRecipe(pump());
        Bukkit.getServer().addRecipe(hydrationChestplate1());
        Bukkit.getServer().addRecipe(hydrationChestplate2());
        Bukkit.getServer().addRecipe(hydrationChestplate3());
    }

    public ShapelessRecipe leafBall() {
        ShapelessRecipe recipe = new ShapelessRecipe(RecipeItems.LEAF_BALL);
        recipe.addIngredient(9, Material.SAPLING);
        return recipe;
    }

    public ShapelessRecipe slimeBall() {
        ShapelessRecipe recipe = new ShapelessRecipe(new ItemStack(Material.SLIME_BALL));
        recipe.addIngredient(4, Material.VINE);
        return recipe;
    }

    public ShapedRecipe nametag() {
        ShapedRecipe recipe = new ShapedRecipe(new ItemStack(Material.NAME_TAG));
        recipe.shape(
                "PS",
                "P ");
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('S', Material.STRING);
        return recipe;
    }

    public ShapedRecipe campfire() {
        ShapedRecipe recipe = new ShapedRecipe(RecipeItems.CAMPFIRE);
        recipe.shape(
                "SSS",
                "SLS",
                "SLS");
        recipe.setIngredient('L', Material.LOG);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }

    public ShapedRecipe advancedClock() {
        ShapedRecipe recipe = new ShapedRecipe(RecipeItems.ADVANCED_CLOCK);
        recipe.shape(
                "GDG",
                "ROR",
                " C ");
        recipe.setIngredient('R', Material.REDSTONE);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('D', Material.DAYLIGHT_DETECTOR);
        recipe.setIngredient('O', Material.WATCH);
        recipe.setIngredient('C', Material.REDSTONE_COMPARATOR);
        return recipe;
    }

    public ShapedRecipe pump() {
        ShapedRecipe recipe = new ShapedRecipe(RecipeItems.PUMP);
        recipe.shape(
                " L ",
                "LDL",
                " R ");
        recipe.setIngredient('L', Material.LEASH);
        recipe.setIngredient('D', Material.DISPENSER);
        recipe.setIngredient('R', Material.REDSTONE);
        return recipe;
    }

    public ShapedRecipe hydrationChestplate1() {
        ShapedRecipe recipe = new ShapedRecipe(RecipeItems.HYDRATION_CHEST_1);
        recipe.shape(
                "III",
                "IDI",
                "IRI");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('D', Material.DISPENSER);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        return recipe;
    }

    public ShapedRecipe hydrationChestplate2() {
        ShapedRecipe recipe = new ShapedRecipe(RecipeItems.HYDRATION_CHEST_2);
        recipe.shape(
                "OIO",
                "BDB",
                "ORO");
        recipe.setIngredient('B', Material.BUCKET);
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setIngredient('O', Material.IRON_INGOT);
        recipe.setIngredient('D', Material.IRON_CHESTPLATE);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        return recipe;
    }

    public ShapedRecipe hydrationChestplate3() {
        ShapedRecipe recipe = new ShapedRecipe(RecipeItems.HYDRATION_CHEST_3);
        recipe.shape(
                "III",
                "IDI",
                "IRI");
        recipe.setIngredient('I', Material.DIAMOND);
        recipe.setIngredient('D', Material.IRON_CHESTPLATE);
        recipe.setIngredient('R', Material.REDSTONE_BLOCK);
        return recipe;
    }

    public ShapedRecipe backpack() {
        CustomStack stack = new CustomStack(Material.LEATHER_CHESTPLATE);
        stack.setAmount(1);
        stack.setDurability(0);
        stack.loreAdd("&7When Holding: ", " &8Right Click to open", "", "&7When Wearing: ", " &8Shift + Left Right Click to open");
        stack.setDiplayname("&fSmall Backpack");
        ItemStack item = CustomStack.hideStats(stack);
        item = CustomStack.setNBTTag(item, "setInt", Integer.TYPE, "backpack.level", 1);
        item = CustomStack.setNBTTag(item, "setString", String.class, "backpack.owner", "");

        new Backpack(item);

        ShapedRecipe recipe = new ShapedRecipe(item != null ? item : stack);
        recipe.shape(
                "RLR",
                "LCL",
                "GLG");
        recipe.setIngredient('R', Material.LEASH);
        recipe.setIngredient('L', Material.LEATHER);
        recipe.setIngredient('C', Material.CHEST);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        return recipe;
    }

    public ShapedRecipe backpackStrap() {
        CustomStack stack = new CustomStack(Material.LEASH);
        stack.setAmount(1);
        stack.setDurability(0);
        stack.loreAdd("&8Used for crafting a backpack");
        stack.setDiplayname("&fBackpack Strap");
        ItemStack item = CustomStack.hideStats(stack);
        item = CustomStack.setNBTTag(item, "setString", String.class, "backpack", "strap");

        ShapedRecipe recipe = new ShapedRecipe(item != null ? item : stack);
        recipe.shape(
                "@  ",
                " @ ",
                "@  ");
        recipe.setIngredient('@', Material.LEATHER);
        return recipe;
    }

    public ShapedRecipe smallWaterBottle() {
        CustomStack stack = new CustomStack(Material.BUCKET);
        stack.setAmount(1);
        stack.setDurability(0);
        stack.loreAdd("&7Uses &7▬▬▬▬▬ &80/5");
        stack.setDiplayname("&fSmall Water Bottle");
        ItemStack item = CustomStack.hideStats(stack);
        item = CustomStack.setNBTTag(item, "setInt", Integer.TYPE, "bottle.uses", 0);
        item = CustomStack.setNBTTag(item, "setInt", Integer.TYPE, "bottle.max", 5);

        ShapedRecipe recipe = new ShapedRecipe(item != null ? item : stack);
        recipe.shape(
                "LIL",
                "LBL",
                " L ");
        recipe.setIngredient('I', Material.STRING);
        recipe.setIngredient('L', Material.LEATHER);
        recipe.setIngredient('B', Material.GLASS_BOTTLE);
        return recipe;
    }

    public ShapedRecipe largeWaterBottle() {
        CustomStack stack = new CustomStack(Material.GLASS_BOTTLE);
        stack.setAmount(1);
        stack.setDurability(0);
        stack.loreAdd("&7Uses &7▬▬▬▬▬▬▬▬▬▬ &80/10");
        stack.setDiplayname("&fLarge Water Bottle");
        ItemStack item = CustomStack.hideStats(stack);
        item = CustomStack.setNBTTag(item, "setInt", Integer.TYPE, "bottle.uses", 0);
        item = CustomStack.setNBTTag(item, "setInt", Integer.TYPE, "bottle.max", 10);

        ShapedRecipe recipe = new ShapedRecipe(item != null ? item : stack);
        recipe.shape(
                "LIL",
                "LBL",
                " L ");
        recipe.setIngredient('I', Material.IRON_INGOT);
        recipe.setIngredient('L', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.BUCKET);
        return recipe;
    }
}
