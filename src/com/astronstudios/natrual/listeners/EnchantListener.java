package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.recipes.EnchantmentAllowance;
import com.astronstudios.natrual.recipes.EnchantmentsManager;
import com.astronstudios.natrual.util.CustomStack;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class EnchantListener implements Listener {

    private final Random random = new Random();
    private final String[] content = EnchantmentAllowance.toStringArray();

    @EventHandler
    public void on(EnchantItemEvent event) {
        final String type = event.getItem().getType().toString();
        final int ul = event.getExpLevelCost();
        ItemStack stack = event.getItem();
        for (String s : content) {
            if (!type.contains(s)) continue;
            int chance = random.nextInt(100);
            int lvlChance = random.nextInt(100);
            int lvl = ul >= 30 && lvlChance < 30 ? 3 : ul >= 8 && lvlChance < 50 ? 2 : 1;
            if (chance <= EnchantmentsManager.HYDRATION_HELPER.getRarity()) {
                event.setCancelled(true);
                stack = EnchantmentsManager.add(EnchantmentsManager.HYDRATION, lvl, stack);
                stack.addUnsafeEnchantments(event.getEnchantsToAdd());
                if (type.contains(EnchantmentAllowance.BOOK.toString())) {
                    stack.setType(Material.ENCHANTED_BOOK);
                    stack = new CustomStack(stack).setDiplayname("&eEnchanted Book");
                }
                event.getInventory().setItem(0, stack);
                return;
            }
        }
    }
}
