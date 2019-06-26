package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ConsumeListener implements Listener {

    private EternalNature plugin;

    public ConsumeListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getItem();
        Material type = stack.getType();

        UserData user = plugin.getEngine().getUserData(player.getUniqueId());
        if (user == null) return;

        if (type == Material.POTION) {
            PotionMeta meta = (PotionMeta) stack.getItemMeta();
            if (meta != null) {
                PotionType potionType = meta.getBasePotionData().getType();
                user.hydrate(5);
            }
        }
        else if (type == Material.MILK_BUCKET) {
            user.hydrate(7);
        }
    }
}
