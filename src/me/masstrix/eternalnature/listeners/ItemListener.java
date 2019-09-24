package me.masstrix.eternalnature.listeners;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.core.world.AgingItemWorker;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class ItemListener implements Listener {

    private EternalNature plugin;

    public ItemListener(EternalNature plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(ItemSpawnEvent event) {
        Item item = event.getEntity();
        boolean plant = plugin.getEngine().getAutoPlanter().attemptToAddItem(item);
        if (!plant) {
            AgingItemWorker worker = (AgingItemWorker) plugin.getEngine().getWorker(AgingItemWorker.class);
            worker.attemptToAddItem(item);
        }
    }
}
