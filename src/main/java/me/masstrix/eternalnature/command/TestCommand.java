package me.masstrix.eternalnature.command;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.entity.shadow.ItemSlot;
import me.masstrix.eternalnature.core.entity.shadow.ShaArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This command is intended for test uses only for dev work and testing new features.
 */
public class TestCommand {

    public void execute(Player player, String[] args) {

        ShaArmorStand armorStand = new ShaArmorStand(player.getLocation());
        armorStand.setSlot(ItemSlot.HEAD, new ItemStack(Material.AZALEA));
        armorStand.setSlot(ItemSlot.CHEST, new ItemStack(Material.CHAINMAIL_CHESTPLATE));
        armorStand.setSlot(ItemSlot.FEET, new ItemStack(Material.IRON_BOOTS));
        armorStand.setCustomName("working");
        armorStand.setCustomNameVisible(true);
        armorStand.setSmall(true);

        for (Player p : Bukkit.getOnlinePlayers())
            armorStand.sendTo(p);

        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                foo(this, i++, armorStand);
            }
        }.runTaskTimer(EternalNature.getPlugin(EternalNature.class), 20 * 3, 20);
    }

    private void foo(BukkitRunnable r, int id, ShaArmorStand armorStand) {
        int task = 0;
        if (id == task++) {
            armorStand.setSmall(false);
            armorStand.setCustomNameVisible(false);
        }
        if (id == task++) {
            armorStand.setSmall(true);
            armorStand.setArms(true);
        }
        else if (id == task++) {
            armorStand.setSlot(ItemSlot.MAINHAND, new ItemStack(Material.AMETHYST_BLOCK));
            armorStand.setSlot(ItemSlot.CHEST, new ItemStack(Material.GOLDEN_CHESTPLATE));
        }
        else if (id == task++) {
            armorStand.setMarker(true);
        }
        else if (id == task++) {
            armorStand.move(0, 0.5, 0);
        }
        else if (id == task++) {
            armorStand.move(0, 0.5, 0);
        }
        else if (id == task++) {
            armorStand.setOnFire(true);
        }
        else if (id == task++) {
            armorStand.setFrozenTicks(100);
        }
        else if (id == task++) {
            armorStand.setCustomName("wooooo");
            armorStand.setCustomNameVisible(true);
        }
        else if (id == task++) {
            armorStand.setInvisible(true);
        }
        else if (id == task++) {
            r.cancel();
            armorStand.remove();
        }
    }
}
