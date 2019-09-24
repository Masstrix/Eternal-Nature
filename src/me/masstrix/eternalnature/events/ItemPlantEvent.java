package me.masstrix.eternalnature.events;

import me.masstrix.eternalnature.core.world.PlantType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;

public class ItemPlantEvent extends EternalEvent implements Cancellable {

    private boolean cancelled;
    private Location loc;
    private PlantType plantType;
    private Material material;

    public ItemPlantEvent(Location loc, PlantType type, Material material) {
        this.loc = loc;
        this.plantType = type;
        this.material = material;
    }

    public Location getLocation() {
        return loc;
    }

    public PlantType getPlantType() {
        return plantType;
    }

    public Material getItemType() {
        return material;
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
