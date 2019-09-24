package me.masstrix.eternalnature.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EternalEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
