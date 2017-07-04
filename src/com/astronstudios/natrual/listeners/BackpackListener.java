package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.items.Backpack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BackpackListener implements Listener {

    private List<Backpack> open = new ArrayList<>();

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction().toString().toLowerCase().contains("right") || !player.isSneaking()) return;
        if (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType() != Material.LEATHER_CHESTPLATE) return;
        ItemStack backpack = player.getInventory().getChestplate();

        Backpack pack = new Backpack(backpack);
        if (!pack.isValid()) return;
        open.add(pack);
        player.openInventory(pack.getInventory());
        event.setCancelled(true);
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        List<Backpack> o = new ArrayList<>();
        Player player = (Player) event.getWhoClicked();

        for (Backpack b : open) {
            Inventory bInv = b.getInventory();

            if (bInv.getViewers().contains(player)) {

                System.out.print("match");

                b.setContents(event.getInventory().getContents());
                b.setItem(event.getSlot(), event.getInventory().getItem(event.getSlot()));
                System.out.print(event.getInventory().getContents());

                //b.setItem(event.getRawSlot(), event.getCurrentItem());
                b.save();
                //event.getWhoClicked().getInventory().setChestplate(b.getBackpack());
            }

            if (b.getInventory().getViewers().size() >= 0) {
                o.add(b);
            }
        }

        open = o;
    }
}
