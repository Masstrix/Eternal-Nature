package me.masstrix.eternalnature.core.block;

import me.masstrix.eternalnature.core.item.SkullIndex;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.TileEntitySkull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;

public class Pineapple {

    // 0 1 2
    private int age;
    private Location location;

    public Pineapple(Location loc) {
        this.location = loc;
    }

    public void grow() {
        age++;
        updateFruit();
    }

    public void setAge(int age) {
        this.age = age;
        updateFruit();
    }

    private void updateFruit() {
        Block pineapple = location.clone().add(0, 1, 0).getBlock();
        if (age == 0) {
            pineapple.setType(Material.AIR);
        } else {
            pineapple.setType(Material.PLAYER_HEAD);
            Skull skull = (Skull) pineapple.getState();
            TileEntitySkull skullE = (TileEntitySkull) ((CraftWorld) skull.getWorld()).getHandle().getTileEntity(new BlockPosition(skull.getX(), skull.getY(), skull.getZ()));
            skullE.setGameProfile(age > 1 ? SkullIndex.PINEAPPLE_MATURE.getProfile() : SkullIndex.PINEAPPLE_IMMATURE.getProfile());
            skullE.update();
        }

        Block stem = location.getBlock();
        stem.setType(Material.SUNFLOWER);
    }
}
