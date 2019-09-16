package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
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
        final int delay = (20 * 60);
        final int rangeOut = 20;
        final int rangeHeight = 5;
        final int scans = 2;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Block s = player.getLocation().getBlock();

                    for (int i = 0; i < scans; i++) {
                        int x = MathUtil.randomInt(rangeOut * 2) - rangeOut;
                        int z = MathUtil.randomInt(rangeOut * 2) - rangeOut;
                        int y = MathUtil.randomInt(rangeHeight * 2) - rangeHeight;

                        Block block = s.getWorld().getBlockAt(x + s.getX(), y + s.getY(), z + s.getZ());
                        BlockData data = block.getBlockData();
                        if (!(data instanceof Leaves)) continue;

                        Leaves leaves = (Leaves) data;
                        if (leaves.isPersistent()) continue; // Ignore player placed leaves

                        // Drop sapling if air is underneath leaf block
                        if (block.getRelative(0, -1, 0).isPassable()) {
                            ItemStack drop = (ItemStack) block.getDrops().toArray()[0];
                            block.getWorld().dropItem(block.getLocation().add(0.5, -0.3, 0.5), drop);
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
