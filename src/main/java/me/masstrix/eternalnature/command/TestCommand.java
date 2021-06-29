package me.masstrix.eternalnature.command;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.util.BlockScanner;
import me.masstrix.eternalnature.util.BlockScannerTask;
import me.masstrix.eternalnature.util.Direction;
import me.masstrix.eternalnature.util.LiquidFlow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

/**
 * This command is intended for test uses only for dev work and testing new features.
 */
public class TestCommand {

    private BukkitTask task;

    public void execute(Player player, String[] args) {


        BlockScannerTask t = block -> {
            if (block.getType() != Material.WATER) return;

            Levelled data = (Levelled) block.getBlockData();
            Direction flow = LiquidFlow.getFlowDir(block, false);

            Location center = block.getLocation().add(0.5, ((float) data.getLevel() / (float) data.getMaximumLevel()) + 1F, 0.5);

            if (flow == LiquidFlow.SOURCE) {
                block.getWorld().spawnParticle(Particle.SCRAPE, center, 1, 0, 0, 0, 0);
                return;
            }

            if (flow == Direction.UNKNOWN) {
                block.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, center, 1, 0, 0, 0, 0);
                return;
            }

            if (flow == Direction.NONE) {
                block.getWorld().spawnParticle(Particle.TOTEM, center, 1, 0, 0, 0, 0);
                return;
            }

            Vector dirAdd = flow.asVector().normalize().multiply(0.1);


            for (int i = 0; i < 5; i++) {
                block.getWorld().spawnParticle(i == 0 ? Particle.SOUL_FIRE_FLAME : Particle.FLAME, center, 1, 0, 0, 0, 0);
                center.add(dirAdd);
            }
        };

        BlockScanner s = new BlockScanner(EternalNature.getPlugin(EternalNature.class))
                .setScanScale(5, 3)
                .setFidelity(1)
                .setHeightOffset(BlockScanner.Position.DOWN)
                .setLocation(player.getLocation());

        if (task != null && !task.isCancelled()) {
            task.cancel();
        } else {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    s.setLocation(player.getLocation());
                    s.scan(t);
                }
            }.runTaskTimer(EternalNature.getPlugin(EternalNature.class), 0, 5);
        }
    }
}
