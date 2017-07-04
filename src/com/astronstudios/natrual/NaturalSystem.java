package com.astronstudios.natrual;

import com.astronstudios.natrual.listeners.DeathListener;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class NaturalSystem {

    private Random random = new Random();

    public NaturalSystem(NaturalEnvironment ne) {
        new BukkitRunnable() {
            int damageTick = 0;
            @Override
            public void run() {
                damageTick = damageTick > 20 ? 0 : damageTick++;
                for (User t : ne.getUsers().values()) {
                    Player player = Bukkit.getPlayer(t.getUser());
                    if (player == null) continue;

                    if (damageTick == 0 && random.nextInt(70) + 1 < (t.getTemp() / 90 > 5 ? 5 : t.getTemp() / 90)){
                        t.saturate(-1);
                    }

                    if ((t.getTemp() > 75 || t.getTemp() < -12) && random.nextInt(70) + 1 < 4) {
                        if (player.getHealth() - 0.5 >= 0) {
                            player.damage(0.5, null);
                        } else {
                            DeathListener.kill(player, "%s let the temperature get to them");
                            ne.get(player.getUniqueId()).reset();
                        }
                    }

                    if (t.getThirst() < 2 && damageTick % (random.nextInt(2) + 3) == 0) {
                        if (player.getHealth() - 1.5 >= 0) {
                            player.damage(1.5, null);
                        } else {
                            DeathListener.kill(player, "%s died from dehydration");
                            ne.get(player.getUniqueId()).reset();
                        }
                    }
                }
            }
        }.runTaskTimer(ne, 0, 1);

        new BukkitRunnable() {
            boolean b = true;

            @Override
            public void run() {
                for (User t : ne.getUsers().values()) {
                    t.updateTemp();
                    Player player = Bukkit.getPlayer(t.getUser());
                    if (player == null) continue;

                    String bar = "";
                    for (double i = 0; i < 20; i++) {
                        bar += i == t.getThirst() ? "\u00A7b◯" : i < t.getThirst() ? "\u00A7b●" : "\u00A77◌";
                    }

                    String h2o = (t.getThirst() < 5 ? b ? "\u00A7cH²O " : "\u00A7fH²O " : "\u00A7fH²O ") + bar;

                    double o = t.getTemp();

                    String tem = "\u00A7" + (o > 70 ? (b ? "c" : "f")
                            : o >= 30 ? "e"
                            : o < -8 ? (b ? "c" : "f")
                            : o < 10 ? "f" : o < 0 ? "b"
                            : "a") + String.format("%.1f°", o);

                    sendActionBar(player, h2o + "  \u00A77-  " + tem);
                }
                b = !b;
            }
        }.runTaskTimer(ne, 5, 5);
    }

    private void sendActionBar(Player p, String msg) {
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }
}
