package com.astronstudios.natrual;

import com.astronstudios.natrual.listeners.*;
import com.astronstudios.natrual.recipes.CraftRecipes;
import com.astronstudios.natrual.recipes.EnchantmentsManager;
import com.astronstudios.natrual.recipes.SmeltRecipes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
* TODO: Item weight
* TODO: Catch on fire if player temp > 150
* TODO: Poison enchantment
* TODO: Chunk loader
* TODO: Magic mushroom (turns normal cow into magic)
* TODO: Wind
* */

public final class NaturalEnvironment extends JavaPlugin implements Listener {

    private static NaturalEnvironment instance;
    private static Map<UUID, User> users = new HashMap<>();

    public static NaturalEnvironment getInstance() {
        return instance;
    }

    public static void main(String[] a) {
        TemperatureMap t = new TemperatureMap();
        for (int i = 0; i <= 60; i++) System.out.print(i + ": " + t.getHeight().get(i) + "\n");
    }

    public void add(UUID uuid) {
        if (!users.containsKey(uuid)) {
            users.put(uuid, get(uuid));
        }
    }

    public User get(UUID uuid) {
        if (!users.containsKey(uuid)) {
            int t = 20, td = 400;
            double d = 0, temp = 0;
            if (getConfig().contains(uuid.toString())) {
                t = getConfig().getConfigurationSection(uuid.toString()).getInt("thirst");
                td = getConfig().getConfigurationSection(uuid.toString()).getInt("thirstD");
                d = getConfig().getConfigurationSection(uuid.toString()).getDouble("walked");
                temp = getConfig().getConfigurationSection(uuid.toString()).getDouble("temp");
            }
            users.put(uuid, new User(uuid, t, d, td, temp));
        }
        return users.get(uuid);
    }

    public Map<UUID, User> getUsers() {
        return users;
    }

    public void remove(UUID uuid) {
        if (users.containsKey(uuid)) {
            users.get(uuid).save();
            users.remove(uuid);
        }
    }

    public void registerListener(Listener... listener) {
        PluginManager manager = Bukkit.getPluginManager();
        for (Listener l : listener)
            manager.registerEvents(l, this);
    }

    public void onEnable() {
        instance = this;

        registerListener(this, new WalkListener(), new RespawnListener(), new QuitListener(), new JoinListener(),
                new DeathListener(), new InventoryListener(), new ConsumeListener(), new BackpackListener(),
                new EnchantListener(), new InteractListener(this), new DamageListener());

        new EnchantmentsManager();
        new NaturalSystem(this);
        new CraftRecipes().register();
        new SmeltRecipes();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getInventory().setHeldItemSlot(5);
        }

        try {
            File f = new File(getDataFolder(), "config.yml");
            if (!f.exists()) saveConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
