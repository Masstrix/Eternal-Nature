package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.util.CustomStack;
import com.astronstudios.natrual.NaturalEnvironment;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class ConsumeListener implements Listener {

    @EventHandler
    public void on(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItem();
        Material type = stack.getType();
        short dur = stack.getDurability();

        if (type == Material.POTION && dur == 0) {
            NaturalEnvironment.getInstance().get(player.getUniqueId()).saturate(3);

            Object current = CustomStack.getNBTTag(stack, "bottle.uses");

            if (current != null && stack.hasItemMeta()) {

                int l = ((int) current) - 1;
                int max = ((int) CustomStack.getNBTTag(stack, "bottle.max"));

                if (l < 0) l = 0;

                String tag = "";
                ItemMeta meta = stack.getItemMeta();
                for (int i = 0; i < max; i++) tag += i < l ? "&b▬" : "&7▬";
                meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', "&fUses " + tag + "&8 " + l + "/" + max)));
                stack.setItemMeta(meta);

                if (l <= 0) {
                    stack.setType(Material.GLASS_BOTTLE);
                } else {
                    stack.setType(Material.POTION);
                    stack.setDurability((short) 0);
                    NaturalEnvironment.getInstance().get(player.getUniqueId()).saturate(3);
                }

                stack = CustomStack.setNBTTag(stack, "setInt", Integer.TYPE, "bottle.uses", l);

                event.setCancelled(true);
                ItemStack stack1 = player.getInventory().getItemInMainHand();
                ItemStack stack2 = player.getInventory().getItemInMainHand();

                if (stack1.getType() == type) {
                    player.getInventory().setItemInMainHand(stack);
                } else if (stack2.getType() == type) {
                    player.getInventory().setItemInOffHand(stack);
                }
            }
        }
        else if (type == Material.POTION && dur > 0) {
            NaturalEnvironment.getInstance().get(player.getUniqueId()).saturate(4);
        }
        else if (type == Material.MILK_BUCKET) {
            NaturalEnvironment.getInstance().get(player.getUniqueId()).saturate(5);
        }
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItem();
        if (stack == null) return;
        int burntime = 20 * 15;

        if (event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (stack.getType() == Material.WATER_BUCKET) {
                stack.setType(Material.BUCKET);
                NaturalEnvironment.getInstance().get(player.getUniqueId()).saturate(7);
                player.setFireTicks(0);
            }
            else if (stack.getType() == Material.LAVA_BUCKET) {
                stack.setType(Material.BUCKET);
                NaturalEnvironment.getInstance().get(player.getUniqueId()).saturate(4);
                player.setFireTicks(burntime);
            }
        }
    }
}
