package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.core.EternalWorker;
import me.masstrix.eternalnature.core.render.LeafEffect;
import me.masstrix.eternalnature.util.CuboidScanner;
import me.masstrix.eternalnature.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class LeafEmitter implements EternalWorker {

    private final int DEFAULT_SCAN_DELAY = 20 * 5;
    private EternalNature plugin;
    private Set<Location>  locations = new HashSet<>();
    private Set<LeafEffect> effects = new HashSet<>();
    private BukkitTask updater, spawner;

    public LeafEmitter(EternalNature plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if (updater != null)
            updater.cancel();
        updater = new BukkitRunnable() { // Auto scan around players for new leaves.
            int ticks = DEFAULT_SCAN_DELAY;
            int passed = 0;
            @Override
            public void run() {
                if (passed++ >= ticks) {
                    passed = 0;
                    if (!plugin.getSystemConfig().isEnabled(ConfigOption.LEAF_EFFECT)) return;
                    scan();
                    int online = Bukkit.getOnlinePlayers().size();
                    if (online > 20) {
                        ticks = (int) Math.floor(online / 5) + DEFAULT_SCAN_DELAY;
                    }
                }
            }
        }.runTaskTimer(plugin, 10, 1);

        if (spawner != null)
            spawner.cancel();
        spawner = new BukkitRunnable() { // Spawn leaf effects in valid locations found.
            @Override
            public void run() {
                Set<LeafEffect> dead = new HashSet<>();
                for (LeafEffect effect : effects) {
                    effect.tick();
                    if (!effect.isAlive())
                        dead.add(effect);
                }
                effects.removeAll(dead);
                if (!plugin.getSystemConfig().isEnabled(ConfigOption.LEAF_EFFECT)) return;
                for (Location loc : locations) {
                    if (MathUtil.chance(300)) {
                        effects.add(new LeafEffect(plugin, loc));
                    }
                }
            }
        }.runTaskTimer(plugin, 30, 1);
    }

    /**
     * Scans an area around all players and checks to see if there are any valid leaves to
     * spawn leafs from.
     */
    public void scan() {
        locations.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            new CuboidScanner(6, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                    (CuboidScanner.CuboidTask) (x, y, z) -> {
                Location blockLoc = new Location(loc.getWorld(), x, y, z);
                        Block block = blockLoc.getBlock();
                        Block below = block.getRelative(BlockFace.DOWN);
                        if (block.getBlockData() instanceof Leaves && below.isPassable()) {
                            locations.add(blockLoc.add(0.5, 0, 0.5));
                        }
            }).start();
        }
    }

    /**
     * Clears all effects and ends all running tasks.
     */
    @Override
    public void end() {
        updater.cancel();
        spawner.cancel();
        locations.clear();
        effects.forEach(LeafEffect::remove);
        effects.clear();
    }
}
