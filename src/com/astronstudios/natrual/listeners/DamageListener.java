package com.astronstudios.natrual.listeners;

import com.astronstudios.natrual.items.CampFire;
import com.astronstudios.natrual.util.CustomStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class DamageListener implements Listener {

    @EventHandler
    public void on(EntityDamageEvent event) {
        Entity e = event.getEntity();
        if (e instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) e;
            ItemStack stack = frame.getItem();
            Object o = CustomStack.getNBTTag(stack, "advancedclock");
            if (o == null) return;
            frame.setItem(new CustomStack(stack).setDiplayname("&eAdvanced Clock"));
        }

        if (e instanceof ArmorStand) {
            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK)
                CampFire.remove((ArmorStand) e);
        }
    }
}
