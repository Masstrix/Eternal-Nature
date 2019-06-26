package me.masstrix.eternalnature.core.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.UUID;

public enum SkullIndex {

    PINEAPPLE_IMMATURE("4853652beb48bce17ed1f368d9b89893b62c2491c865dbb6eb8e52582c48e38"),
    PINEAPPLE_MATURE("d7eddd82e575dfd5b7579d89dcd2350c991f0483a7647cffd3d2c587f21"),

    APPLE_IMMATURE("63e8659478dd28b1ade6ebe7d3e1d6758e219f438db784a5addeda86ed1a38a"),
    APPLE("e2b35bda5ebdf135f4e71ce49726fbec5739f0adedf01c519e2aea7f51951ea2");

    private String id;
    private GameProfile profile;
    private ItemStack stack;

    SkullIndex() {
        this("");
    }

    SkullIndex(String id) {
        this.id = id;
        this.stack = new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta = (SkullMeta) this.stack.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), this.name());
        profile.getProperties().clear();
        PropertyMap map = profile.getProperties();
        byte[] encoded = Base64.encodeBase64(String.format("{\"textures\":{\"SKIN\":" +
                "{\"url\":\"http://textures.minecraft.net/texture/%s\"}}}", this.id).getBytes());
        map.put("textures", new Property("textures", new String(encoded)));

        this.profile = profile;

        try {
            assert meta != null;
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        this.stack.setItemMeta(meta);
    }

    public GameProfile getProfile() {
        return profile;
    }

    public String getId() {
        return id;
    }

    public ItemStack asItem() {
        return stack;
    }
}
