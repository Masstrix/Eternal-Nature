package com.astronstudios.natrual.events;


import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDehydrateDeathEvent extends Event {

    private static HandlerList list = new HandlerList();

    private Player player;

    public PlayerDehydrateDeathEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }

    public static HandlerList getHandlerList() {
        return list;
    }
}
