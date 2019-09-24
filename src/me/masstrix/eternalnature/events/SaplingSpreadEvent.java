package me.masstrix.eternalnature.events;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;

public class SaplingSpreadEvent extends EternalEvent implements Cancellable {

    private boolean cancelled;
    private Location loc;

    public SaplingSpreadEvent(Location loc) {
        this.loc = loc;
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
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
