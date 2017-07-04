package com.astronstudios.natrual;

import com.astronstudios.natrual.events.PlayerDehydrateEvent;
import com.astronstudios.natrual.items.CampFire;
import com.astronstudios.natrual.recipes.EnchantmentsManager;
import com.astronstudios.natrual.util.CustomStack;
import net.minecraft.server.v1_11_R1.NBTTagInt;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class User {

    private static FileConfiguration config = NaturalEnvironment.getInstance().getConfig();
    private static Random random = new Random();

    private double walked = 0, temp = 0;
    private int thirst = 20, thirstD = 0;
    private UUID uuid;

    public User(UUID uuid, int thirst, double walked, int thirstD, double temp) {
        this.uuid = uuid;
        this.thirst = thirst;
        this.walked = walked;
        this.thirstD = thirstD;
        this.temp = temp;
    }

    public UUID getUser() {
        return this.uuid;
    }

    public int getThirst() {
        return thirst;
    }

    public double getTemp() {
        return temp;
    }

    private List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = new ArrayList<>();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    public int updateTemp() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return Integer.MAX_VALUE;
        double temp_to = 0;
        int b = 0, t = 0;

        List<TemperatureValue> biomes = new ArrayList<>();
        Map<TemperatureValue, Double> factors = new HashMap<>();

        for (Block m : getNearbyBlocks(player.getLocation(), 7)) {
            String y = m.getType().name().toLowerCase();
            String bi = m.getBiome().toString().replaceAll("MUTATED_", "");

            for (TemperatureValue tv : TemperatureValue.values()) {
                if (!biomes.contains(tv)
                        && tv.type == TemperatureType.BIOME
                        && bi.contains(tv.toString())) {
                    t++;
                    b += tv.attr;
                    biomes.add(tv);
                    continue;
                }
                if (tv.type != TemperatureType.BLOCK) continue;
                if (!y.contains(tv.name)) continue;
                if (factors.containsKey(tv)) {
                    if (factors.get(tv) > m.getLocation().distance(player.getLocation()))
                        factors.put(tv, m.getLocation().distance(player.getLocation()));
                    continue;
                }
                factors.put(tv, m.getLocation().distance(player.getLocation()));
            }
        }

        for (TemperatureValue v : factors.keySet()) {
            double d = v.attr / (factors.get(v) / 2);
            temp_to += d;
        }

        for (Entity e : player.getNearbyEntities(5, 5, 5)) {
            if (e instanceof ArmorStand && CampFire.isLit((ArmorStand) e)) {
                temp_to += TemperatureValue.CAMPFIRE.attr / (e.getLocation().distance(player.getLocation()) / 2);
                break;
            }
        }

        ItemStack[] armor = player.getInventory().getArmorContents();

        ItemStack hand_l = player.getInventory().getItemInOffHand();
        ItemStack hand_r = player.getInventory().getItemInMainHand();

        for (TemperatureValue tv : TemperatureValue.values()) {
            if (tv.type == TemperatureType.ARMOR) {
                for (ItemStack s : armor) {
                    if (s != null) {
                        if (s.containsEnchantment(EnchantmentsManager.HYDRATION)) {
                            double lvl = s.getEnchantmentLevel(EnchantmentsManager.HYDRATION) * 2.35;
                            if (temp_to > 40) {
                                temp_to /= (Math.pow(1.00001D + (lvl / 100000D), (temp_to * 1.03)));
                            }
                        }
                        Object o = CustomStack.getNBTTag(s, "hydration.level");
                        if (o != null) {
                            NBTTagInt tag = (NBTTagInt) o;
                            int i = tag.e();
                            if (i == 3) {
                                if (temp_to > 40) temp_to /= (Math.pow(1.00101, (temp_to * 1.03)));
                            } else if (i == 2) {
                                if (temp_to > 40) temp_to /= (Math.pow(1.0004, (temp_to * 1.03)));
                            } else {
                                if (temp_to > 40) temp_to /= (Math.pow(1.0002, (temp_to * 1.03)));
                            }
                            continue;
                        }
                    }

                    if (s != null && s.getType().toString().equals(tv.toString())) {
                        temp_to += tv.attr;
                    }
                }
            }
            else if (tv.type == TemperatureType.ITEM) {
                if (hand_l != null && hand_l.getType().toString().toLowerCase().contains(tv.name)) temp_to += tv.attr;
                if (hand_r != null && hand_r.getType().toString().toLowerCase().contains(tv.name)) temp_to += tv.attr;
            }
        }

        World world = player.getWorld();

        long time = world.getTime();
        if (world.getEnvironment() == World.Environment.NORMAL)temp_to -= time > 12000L ? (int) (time / 1000) : 0;

        if (t > 0 && b > 0) temp_to += (b / t) < temp_to ? (b / t) / temp_to : (b / t);
        else temp_to += b;

        if (world.isThundering()) temp_to -= 5;

        if (temp != temp_to) {
            double v = (temp - temp_to);
            if (temp_to > 0 || temp_to <= 0) temp -= v / 40;
            else temp += v / 40;
        }
        save();
        return (int) temp_to;
    }

    public void saturate(int v) {
        PlayerDehydrateEvent event = new PlayerDehydrateEvent(Bukkit.getPlayer(this.uuid),
                this.thirst, this.thirst + v > 20 ? 20 : this.thirst + v < 0 ? 0 : this.thirst + v);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;
        this.thirst = event.getTo();
        if (v < 0) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getWorld().spawnParticle(Particle.WATER_SPLASH, player.getEyeLocation(), 15, 0, 0, 0, 0);
            }
        }
        save();
    }

    public void addDistance(double d) {
        this.walked += d;
        if (walked >= thirstD) {
            walked = 0;
            thirstD = random.nextInt(50) + 50;
            saturate(-1);
        }
        save();
    }

    public void reset() {
        saturate(100);
        walked = 0;
        thirstD = random.nextInt(50) + 50;
        temp = updateTemp();
    }

    public void save() {
        config.set(uuid.toString() + ".thirst", thirst);
        config.set(uuid.toString() + ".thirstD", thirstD);
        config.set(uuid.toString() + ".walked", walked);
        config.set(uuid.toString() + ".temp", temp);
        NaturalEnvironment.getInstance().saveConfig();
    }
}
