package me.masstrix.eternalnature.events;

import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;

public class ItemRotEvent extends EternalEvent implements Cancellable {

    private boolean cancelled;
    private Item item;

    public ItemRotEvent(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
