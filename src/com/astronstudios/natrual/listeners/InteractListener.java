package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.items.AdvancedClock;
import com.astronstudios.natrual.items.CampFire;
import com.astronstudios.natrual.util.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class InteractListener implements Listener {

    @EventHandler
    public void on(PlayerInteractEvent event) {
        ItemStack i = event.getItem();
        Block clicked = event.getClickedBlock();
        Player player = event.getPlayer();
        if (clicked == null
                || clicked.getType() == Material.AIR
                || clicked.getWorld().getBlockAt(clicked.getLocation().add(0, 1, 0)).getType()
                != Material.AIR) {
            return;
        }
        if (i == null) return;
        Object current = CustomStack.getNBTTag(i, "campfire");
        if (current == null) return;
        new CampFire(event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5)).spawn();
        event.setCancelled(true);

        if (player.getInventory().getItemInMainHand().isSimilar(i)) {
            i.setAmount(i.getAmount() - 1);
            if (i.getAmount() <= 0) player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            else player.getInventory().setItemInMainHand(i);
        } else {
            i.setAmount(i.getAmount() - 1);
            if (i.getAmount() <= 0) player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            else player.getInventory().setItemInOffHand(i);
        }
    }

    @EventHandler
    public void on(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            if (new AdvancedClock().isValid((ArmorStand) event.getRightClicked())) {
                event.setCancelled(true);
            }
            else if (CampFire.isValid((ArmorStand) event.getRightClicked())) {
                event.setCancelled(true);
                ItemStack i1 = event.getPlayer().getInventory().getItemInMainHand();
                ItemStack i2 = event.getPlayer().getInventory().getItemInMainHand();
                if (i1 != null && i1.getType() == Material.FLINT_AND_STEEL) {
                    i1.setDurability((short) (i1.getDurability() - 1));
                    CampFire.ignite((ArmorStand) event.getRightClicked());
                }
                else if (i2 != null && i2.getType() == Material.FLINT_AND_STEEL) {
                    i2.setDurability((short) (i2.getDurability() - 1));
                    CampFire.ignite((ArmorStand) event.getRightClicked());
                }
            }
        }
    }

    private String getTime(World world) {
        long gameTime = world.getTime(),
                hours = gameTime / 1000 + 6,
                minutes = (gameTime % 1000) * 60 / 1000;
        String ampm = "&eAM";
        if (hours >= 12) {
            hours -= 12;
            ampm = "&ePM";
        }
        if (hours >= 12) {
            hours -= 12;
            ampm = "&eAM";
        }
        if (hours == 0) hours = 12;
        String mm = "0" + minutes;
        mm = mm.substring(mm.length() - 2, mm.length());

        return hours + ":" + mm + " " + ampm;
    }

    public InteractListener(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (Entity e : player.getNearbyEntities(2, 1, 2)) {
                        if (e instanceof ItemFrame) {
                            ItemFrame frame = (ItemFrame) e;
                            for (Entity stand : e.getNearbyEntities(1, 1, 1)) {
                                if (!(stand instanceof ArmorStand)) continue;

                            }
                            /*ItemStack stack = frame.getItem();
                            if (stack == null || stack.getType() == Material.AIR) continue;
                            Object o = CustomStack.getNBTTag(stack, "advancedclock");
                            if (o == null) continue;
                            */
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 5, 5);
    }
}
