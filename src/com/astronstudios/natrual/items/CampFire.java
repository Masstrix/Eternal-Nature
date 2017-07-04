package com.astronstudios.natrual.items;

import com.astronstudios.natrual.util.LightSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.Random;
import java.util.UUID;

public class CampFire {

    private Location location;
    private static final Random random = new Random();

    public CampFire(Location location) {
        this.location = location.add(0, -0.8, 0);
    }

    public static boolean isValid(ArmorStand stand) {
        return stand.getCustomName() != null && stand.getCustomName().startsWith("CAMPFIRE-") && stand.getCustomName().length() > 10;
    }

    public static boolean isLit(ArmorStand stand) {
        return isValid(stand) && stand.getFireTicks() > 0;
    }

    public static void ignite(ArmorStand stand) {
        if (stand.getCustomName().startsWith("CAMPFIRE-") && stand.getCustomName().length() > 10) {
            stand.getNearbyEntities(2, 2, 2).stream().filter(e ->
                    e instanceof ArmorStand).forEach(e -> {
                if (e.getCustomName().equals(stand.getCustomName())) e.setFireTicks(12000);
            });

            Location location = stand.getLocation();

            LightSource ls = new LightSource();
            ls.createLight(location.getWorld(), (int) location.getX(), location.getBlockY() + 2, location.getBlockZ(), 15);
            ls.recalculateLight(location.getWorld(), (int) location.getX(), (int) location.getY() + 2, (int) location.getZ());

            int r = 2;

            for (int x = 0 - r; x < r; x++) {
                for (int z = 0 - r; z < r; z++) {
                    ls.sendChunkUpdate(location.getWorld(),
                            location.getChunk().getX() + x,
                            location.getChunk().getZ() + z,
                            Bukkit.getOnlinePlayers());
                }
            }
        }
    }

    public static void remove(ArmorStand stand) {
        if (isValid(stand)) {

            ItemStack stack = new ItemStack(Material.STICK, random.nextInt(2) + 1);
            Location loc = stand.getLocation().clone();
            loc.getWorld().dropItem(loc.clone().add(0, 1.8, 0), stack);

            stand.getNearbyEntities(2, 2, 2).stream().filter(e ->
                    e instanceof ArmorStand).forEach(e -> {
                if (e.getCustomName().equals(stand.getCustomName())) e.remove();
            });

            try {
                stand.remove();
            } catch (Exception ignore) {}

            int r = 5;

            LightSource ls = new LightSource();
            for (int x = 0 - r; x < r; x++) {
                for (int y = 0 - r; y < r; y++) {
                    for (int z = 0 - r; z < r; z++) {
                        ls.deleteLight(loc.getWorld(), ((int) loc.getX()) + x, ((int) loc.getX()) + y, ((int) loc.getX()) + z);
                        ls.recalculateLight(loc.getWorld(), ((int) loc.getX()) + x, ((int) loc.getY()) + y, ((int) loc.getZ()) + z);
                    }
                }
            }

            for (int x1 = -2; x1 < 2; x1++) {
                for (int z1 = -2; z1 < 2; z1++) {
                    ls.sendChunkUpdate(loc.getWorld(),
                            loc.getChunk().getX() + x1,
                            loc.getChunk().getZ() + z1,
                            Bukkit.getOnlinePlayers());
                }
            }
        }
    }

    public void spawn() {
        UUID u = UUID.randomUUID();
        for (int i = 0; i < 4; i++) {
            getBuild(u);
            location.setYaw(location.getYaw() + 45);
        }
    }

    private ArmorStand getBuild(UUID uuid) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setArms(true);
        stand.setBasePlate(false);
        stand.setLeftArmPose(new EulerAngle(0, 0, 2.8));
        stand.setRightArmPose(new EulerAngle(0, 0, -2.8));
        stand.setLeftLegPose(new EulerAngle(0, 0, 2.8));
        stand.setRightLegPose(new EulerAngle(0, 0, -2.8));
        stand.setGravity(false);
        stand.setCustomName("CAMPFIRE-" + uuid.toString());
        stand.setInvulnerable(true);
        return stand;
    }

    public static void destroy() {

    }
}
