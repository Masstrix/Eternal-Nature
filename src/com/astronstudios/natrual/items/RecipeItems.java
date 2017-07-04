package com.astronstudios.natrual.items;

import com.astronstudios.natrual.util.CustomStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class RecipeItems {

    public static ItemStack HYDRATION_CHEST_1;
    public static ItemStack HYDRATION_CHEST_2;
    public static ItemStack HYDRATION_CHEST_3;
    public static ItemStack PUMP;
    public static ItemStack ADVANCED_CLOCK;
    public static ItemStack CAMPFIRE;
    public static ItemStack GOOP;
    public static ItemStack LEAF_BALL;

    public RecipeItems load() {
        HYDRATION_CHEST_1 = hydrationChestplateItem1();
        HYDRATION_CHEST_2 = hydrationChestplateItem2();
        HYDRATION_CHEST_3 = hydrationChestplateItem3();
        PUMP              = pump();
        ADVANCED_CLOCK    = advancedClock();
        CAMPFIRE          = campfire();
        GOOP              = goop();
        LEAF_BALL         = leafBall();
        return this;
    }

    private ItemStack leafBall() {
        CustomStack stack = new CustomStack(Material.LEAVES);
        stack.setAmount(1);
        stack.setDiplayname("&fLeave Ball");
        ItemStack s = CustomStack.hideStats(stack);
        return CustomStack.setNBTTag(s, "setInt", Integer.TYPE, "leaveball", 1);
    }

    private ItemStack goop() {
        CustomStack stack = new CustomStack(Material.VINE);
        stack.setAmount(1);
        stack.setDiplayname("&fGoop");
        ItemStack s = CustomStack.hideStats(stack);
        return CustomStack.setNBTTag(s, "setInt", Integer.TYPE, "goop", 1);
    }

    private ItemStack campfire() {
        CustomStack stack = new CustomStack(Material.LOG);
        stack.setAmount(1);
        stack.loreAdd("&7Place it anywhere to create light", "&7and heat.");
        stack.setDiplayname("&6Campfire");
        stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemStack s = CustomStack.hideStats(stack);
        return CustomStack.setNBTTag(s, "setInt", Integer.TYPE, "campfire", 1);
    }

    private ItemStack hydrationChestplateItem1() {
        CustomStack stack = new CustomStack(Material.IRON_CHESTPLATE);
        stack.setAmount(1);
        stack.loreAdd("&7Regulates the wearers", "&7temperatures.", "&8Level I");
        stack.setDiplayname("&bHydration Chestplate");
        return CustomStack.setNBTTag(stack, "setInt", Integer.TYPE, "hydration.level", 1);
    }

    private ItemStack hydrationChestplateItem2() {
        CustomStack stack = new CustomStack(Material.IRON_CHESTPLATE);
        stack.setAmount(1);
        stack.loreAdd("&7Regulates the wearers", "&7temperatures.", "&8Level II");
        stack.setDiplayname("&bHydration Chestplate");
        return CustomStack.setNBTTag(stack, "setInt", Integer.TYPE, "hydration.level", 2);
    }

    private ItemStack hydrationChestplateItem3() {
        CustomStack stack = new CustomStack(Material.DIAMOND_CHESTPLATE);
        stack.setAmount(1);
        stack.loreAdd("&7Regulates the wearers", "&7temperatures.", "&8Level III");
        stack.setDiplayname("&bHydration Chestplate");
        return CustomStack.setNBTTag(stack, "setInt", Integer.TYPE, "hydration.level", 3);
    }

    private ItemStack pump() {
        CustomStack stack = new CustomStack(Material.DISPENSER);
        stack.setAmount(1);
        stack.loreAdd("&7Used for crafting hydration", "&7set.");
        stack.setDiplayname("&fPump");
        return CustomStack.setNBTTag(stack, "setInt", Integer.TYPE, "hydration.pump", 1);
    }

    private ItemStack advancedClock() {
        CustomStack stack = new CustomStack(Material.WATCH);
        stack.setAmount(1);
        stack.loreAdd("&7Displays the current world time", "&7if places in an item frame.");
        stack.setDiplayname("&eAdvanced Clock");
        stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        ItemStack s = CustomStack.hideStats(stack);
        return CustomStack.setNBTTag(s, "setBoolean", Boolean.TYPE, "advancedclock", true);
    }
}
