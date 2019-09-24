package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.events.ItemRotEvent;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the rotting of an item. If the item is valid and has a waste product
 * then it will turn into the after a random time between 2 and 4 minutes.
 */
public class AgeRotItem implements AgeItem {

    private Item item;
    private EternalNature plugin;
    private int ticks;
    private int ticksRandom;
    private boolean rotted;

    public AgeRotItem(EternalNature plugin, Item item) {
        this.plugin = plugin;
        this.item = item;
        this.ticksRandom = MathUtil.randomInt(120, 240);

        // Adds ticks if item is already aged.
        if (item.getTicksLived() >= 20 * 60) {
            this.ticks = item.getTicksLived() / 20;
        }
    }

    @Override
    public boolean isValid() {
        return item.isValid();
    }

    @Override
    public boolean isDone() {
        return rotted;
    }

    @Override
    public AgeProcessState tick() {
        if (!isValid()) return AgeProcessState.INVALID;
        if (rotted) return AgeProcessState.COMPLETE;
        if (++ticks < ticksRandom) return AgeProcessState.AGING;
        ItemRotEvent event = new ItemRotEvent(item);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return AgeProcessState.COMPLETE;
        rotted = true;
        ItemStack stack = item.getItemStack();
        stack.setType(AgingItemWorker.getWasteProduct(stack.getType()));
        item.setItemStack(stack);
        item.getWorld().spawnParticle(Particle.REDSTONE, item.getLocation(), 3, 0, 0, 0,
                new Particle.DustOptions(Color.fromRGB(255, 255, 255), 1));
        return AgeProcessState.COMPLETE;
    }
}
