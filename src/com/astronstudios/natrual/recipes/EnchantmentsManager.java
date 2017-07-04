package com.astronstudios.natrual.recipes;

import com.astronstudios.natrual.recipes.enchantments.HydrationEnchantment;
import com.astronstudios.natrual.util.CustomStack;
import com.astronstudios.natrual.util.RomanNumber;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.HashMap;

public class EnchantmentsManager {

    public static final EnchantmentHelper HYDRATION_HELPER = new EnchantmentHelper(30,
            EnchantmentAllowance.HELMET, EnchantmentAllowance.CHESTPLATE, EnchantmentAllowance.LEGGINGS,
            EnchantmentAllowance.BOOTS, EnchantmentAllowance.BOOK);
    public static final Enchantment HYDRATION = new HydrationEnchantment(100);

    @SuppressWarnings("unchecked")
    public EnchantmentsManager() {

        try {
            Field byIdF = Enchantment.class.getDeclaredField("byId");
            Field byNameF = Enchantment.class.getDeclaredField("byName");

            byIdF.setAccessible(true);
            byNameF.setAccessible(true);

            HashMap<Integer, Enchantment> byId = (HashMap<Integer, Enchantment>) byIdF.get(null);
            HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) byNameF.get(null);

            if (byId.containsKey(100)) byId.remove(100);
            if (byName.containsKey(HYDRATION.getName())) byName.remove(HYDRATION.getName());
        } catch (Exception ignored) {}

        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(HYDRATION);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ItemStack add(Enchantment enchantment, int level, ItemStack stack) {
        if (enchantment.getName().equals(HYDRATION.getName())) {
            stack.addEnchantment(enchantment, level);
            return new CustomStack(stack).loreAdd("&7" + enchantment.getName() + " " + RomanNumber.toRoman(level));
        } else {
            stack.addEnchantment(enchantment, level);
            return stack;
        }
    }
}
