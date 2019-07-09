package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class SaplingPlanter implements EternalWorker {

    private EternalNature plugin;
    private SystemConfig config;
    private Set<Item> saplings = new HashSet<>();
    private BukkitTask task;

    public SaplingPlanter(EternalNature plugin) {
        this.plugin = plugin;
        this.config = plugin.getSystemConfig();
    }

    /**
     * Loops through each worlds entities to find any that are a item and
     * a sapling to potentially grow.
     */
    public void findSaplings() {
        if (!config.isAutoPlantSaplings()) return;
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (e instanceof Item) addSapling((Item) e);
            }
        }
    }

    /**
     * Adds a sapling item to the list. If item is not a type of
     * sapling then it is ignored.
     *
     * @param item item being added.
     */
    public void addSapling(Item item) {
        if (isSapling(item)) saplings.add(item);
    }

    /**
     * @param item item to check if is a sapling.
     * @return if the item is a type of sapling.
     */
    private boolean isSapling(Item item) {
        return item.getItemStack().getItemMeta() instanceof Sapling;
    }

    @Override
    public void start() {
        if (!config.isAutoPlantSaplings()) return;
        if (task != null) throw new IllegalCallerException("start() cannot be called more than once.");
        plugin.getLogger().info("Started sapling spreader");
        findSaplings();
        task = new BukkitRunnable() {
            @Override
            public void run() {
                saplings.iterator().forEachRemaining(item -> randomPlant(item));
            }
        }.runTaskTimerAsynchronously(plugin, 20, 20);
    }

    @Override
    public void end() {
        task.cancel();
        task = null;
    }

    /**
     * Randomly plant a sapling by chance. This sapling has to has been sitting for 30
     * seconds before a 20% chance of it being placed every second is applied. This slight
     * randomness makes it slightly more organic than a specific time.
     *
     * @param item item being ticked.
     */
    private void randomPlant(Item item) {
        if (isSapling(item)&& item.getTicksLived() > 600 && MathUtil.chance(20)) {
            saplings.remove(item);
            Material saplingType = item.getItemStack().getType();
            Block ground = item.getLocation().add(0, -1, 0).getBlock();
            Material type = ground.getType();
            if (type == Material.GRASS_BLOCK || type == Material.DIRT) {
                Block block = item.getLocation().getBlock();
                item.remove();
                block.setType(saplingType);
            }
        }
    }
}
