package me.masstrix.eternalnature.events;

import me.masstrix.eternalnature.api.Leaf;
import org.bukkit.event.Cancellable;

public class LeafSpawnEvent extends EternalEvent implements Cancellable {

    private boolean cancelled;
    private Leaf leaf;

    public LeafSpawnEvent(Leaf leaf) {
        this.leaf = leaf;
    }

    public Leaf getLeaf() {
        return leaf;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
