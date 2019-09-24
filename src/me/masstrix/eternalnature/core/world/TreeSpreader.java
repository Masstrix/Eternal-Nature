package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.events.SaplingSpreadEvent;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Handles randomly spreading palings from trees.
 */
public class TreeSpreader implements EternalWorker {

    private BukkitTask task;
    private EternalNature plugin;
    private SystemConfig config;

    public TreeSpreader(EternalNature plugin) {
        this.plugin = plugin;
        config = plugin.getSystemConfig();
    }

    @Override
    public void start() {
        if (!config.isEnabled(ConfigOption.RANDOM_TREE_SPREAD) || task != null) return;
        plugin.getLogger().info("Started Tree Spreader");
        final int delay = (20 * 30);
        final int rangeHeight = 5;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                final int rangeOut = config.getInt(ConfigOption.RANDOM_TREE_SPREAD_RANGE);
                final int scans = config.getInt(ConfigOption.RANDOM_TREE_SPREAD_SCANS);

                for (Player player : Bukkit.getOnlinePlayers()) {
                    Block s = player.getLocation().getBlock();

                    for (int i = 0; i < scans; i++) {
                        int x = MathUtil.randomInt(rangeOut * 2) - rangeOut;
                        int z = MathUtil.randomInt(rangeOut * 2) - rangeOut;
                        int y = MathUtil.randomInt(rangeHeight * 3) - rangeHeight;

                        Block block = s.getWorld().getBlockAt(x + s.getX(), y + s.getY(), z + s.getZ());
                        BlockData data = block.getBlockData();
                        if (!(data instanceof Leaves)) continue;

                        Leaves leaves = (Leaves) data;
                        if (leaves.isPersistent()) continue; // Ignore player placed leaves

                        // Drop sapling if air is underneath leaf block
                        if (block.getRelative(0, -1, 0).isPassable()) {
                            Location loc = block.getLocation().add(0.5, -0.3, 0.5);
                            SaplingSpreadEvent event = new SaplingSpreadEvent(loc);
                            Bukkit.getPluginManager().callEvent(event);
                            if (event.isCancelled()) continue;
                            loc = event.getLocation();
                            Material product = TreeProduct.SAPLING.convert(block.getType());
                            ItemStack drop = new ItemStack(product);
                            block.getWorld().dropItem(loc, drop);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, delay, delay);
    }

    @Override
    public void end() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
