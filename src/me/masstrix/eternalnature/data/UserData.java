package me.masstrix.eternalnature.data;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.api.EternalUser;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.config.StatusRenderMethod;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.world.ChunkData;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.util.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class UserData implements EternalUser {

    private static final int dehydrateChance = 500;
    private static final int dehydrateChanceRange = 50;

    public static final float MAX_THIRST = 20;
    private SystemConfig config;
    private EternalNature plugin;

    private Stopwatch damageTimer = new Stopwatch();
    private Flicker flicker = new Flicker(300);

    private BossBar hydrationBar, tempBar;
    private UUID id;
    private float temperature = 0, tempExact = 0;
    private float hydration = 20; // max is 20
    private float distanceWalked;
    private int distanceNextThirst;

    private boolean nulled = false;

    public UserData(EternalNature plugin, UUID id) {
        this.id = id;
        this.plugin = plugin;
        config = plugin.getSystemConfig();

        Player player = Bukkit.getPlayer(id);
        if (player != null) {
            WorldProvider provider = plugin.getEngine().getWorldProvider();
            WorldData data = provider.getWorld(player.getWorld());
            this.temperature = data.getTemperature(player.getLocation().toVector());
        }
    }

    public UserData(EternalNature plugin, UUID id, float temperature, float tempTo, float hydration) {
        this.id = id;
        this.plugin = plugin;
        this.temperature = temperature;
        this.tempExact = tempTo;
        this.hydration = hydration;
        config = plugin.getSystemConfig();
        distanceNextThirst = MathUtil.randomInt(dehydrateChance, dehydrateChance + dehydrateChanceRange);
    }

    /**
     * Tick the players data. This will do all necessary updates for the player.
     */
    public final void tick() {
        Player player = Bukkit.getPlayer(id);
        if (player == null) return;

        WorldProvider provider = plugin.getEngine().getWorldProvider();
        WorldData data = provider.getWorld(player.getWorld());
        Location loc = player.getLocation();
        if (data != null && config.isEnabled(ConfigOption.TEMP_ENABLED)) {
            ChunkData chunk = data.getChunk(loc.getChunk().getX(), loc.getChunk().getZ());
            data.loadNearby(loc.toVector());

            if (chunk != null) {
                float emission = data.getTemperature(loc.toVector());
                //emission += plugin.getEngine().getTemperatureData().getEmissionValue(TemperatureData.DataTempType.BIOME, loc.getBlock().getBiome().name());

                if (emission != Float.NEGATIVE_INFINITY) {
                    this.tempExact = emission;
                    nulled = false;
                }
                else {
                    nulled = true;
                    //data.calculateArea(id, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                }

                // Change players temperature gradually
                float diff = MathUtil.diff(this.tempExact, this.temperature);
                if (diff > 0.09) {
                    float toAdd = diff / 10;
                    if (this.tempExact > this.temperature) this.temperature += toAdd;
                    else this.temperature -= toAdd;
                }
            } else {
                //data.calculateArea(id, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            }
        }

        // Sweat randomly. Becomes more common the warmer you are.
        if (plugin.getSystemConfig().isEnabled(ConfigOption.TEMP_SWEAT)) {
            if (MathUtil.randomInt((int) (1000 * 1.5)) <= temperature * (temperature * 0.005)) {
                dehydrate(0.25);
            }
        }

        // Handle damage tick if player is dehydrated or to hot/cold
        if (hydration <= 0 && config.isEnabled(ConfigOption.HYDRATION_DAMAGE)) {
            damageTimer.startIfNew();
            if (damageTimer.hasPassed(3000)) {
                damageTimer.start();
                player.damage(1);
                player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, 0.5));
            }
        }

        World world = player.getWorld();

        new CuboidScanner(4, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                (CuboidScanner.CuboidTask) (x, y, z) -> {
            Block block = world.getBlockAt(x, y, z);
            if (!block.isPassable() && block.getType().isSolid() || block.getType() == Material.TORCH) {

                double max = plugin.getEngine().getTemperatureData().getMaxBlockTemp();
                double temp = provider.getWorld(world).getTemperature(x, y, z);
                Color color = new Color(126, 255, 0);

                if (temp != Float.NEGATIVE_INFINITY) {
                    float colorRotate = (float) temp / (float) max;

                    float[] hsbVals = new float[3];
                    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsbVals);
                    // Shift the hue around by 25%
                    color = new Color(Color.HSBtoRGB(0.25f - (0.25f * colorRotate), hsbVals[1], hsbVals[2]));

                    if (temp > 20) {
                        color = new Color(0, 132, 21);
                    }
                    else if (temp > 40) {
                        color = new Color(53, 255, 0);
                    }
                    else if (temp > 40) {
                        color = new Color(255, 240, 0);
                    }
                    else if (temp > 60) {
                        color = new Color(255, 65, 0);
                    }
                    else if (temp > 100) {
                        color = new Color(255, 149, 140);
                    }
                    else if (temp > 200) {
                        color = new Color(255, 255, 255);
                    }
                } else {
                    color = new Color(255, 0, 206); // Unloaded
                }

                player.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.5, 1.2, 0.5),
                        1, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()), 1));
            }
        });//.start();
    }

    /**
     * Called by {@link me.masstrix.eternalnature.core.Renderer} to render all components
     * for the players perspective.
     */
    public final void render() {
        Player player = Bukkit.getPlayer(id);
        if (player == null) return; // Stop it from rendering if player is offline.
        flicker.update();

        // Render user info.
        StringBuilder actionBar = new StringBuilder();
        if (config.isEnabled(ConfigOption.HYDRATION_ENABLED)) {
            if (config.getRenderMethod(ConfigOption.HYDRATION_BAR_STYLE) == StatusRenderMethod.BOSSBAR) {
                if (hydrationBar == null) {
                    hydrationBar = Bukkit.createBossBar("h2-", BarColor.BLUE, BarStyle.SEGMENTED_12);
                    hydrationBar.addPlayer(player);
                }
                hydrationBar.setProgress(hydration / 20);
                int percent = (int) ((hydration / 20F) * 100);
                String flash = hydration <= 4 && config.getBoolean(ConfigOption.HYDRATION_BAR_FLASH)
                        && flicker.isEnabled() ? "c" : "f";
                hydrationBar.setTitle(StringUtil.color("H²O &" + flash + percent + "%"));
            } else {
                if (hydrationBar != null) {
                    hydrationBar.removeAll();
                    hydrationBar = null;
                }
                String flash = hydration <= 4 && config.getBoolean(ConfigOption.HYDRATION_BAR_FLASH)
                        && flicker.isEnabled() ? "c" : "f";
                StringBuilder h20 = new StringBuilder("\u00A7" + flash + "H²O ");
                float mid = Math.round(hydration / 2);
                for (int i = 0; i < 10; i++) {
                    if (i < mid) {
                        h20.append("\u00A7b●");
                    } else if (i > mid) {
                        h20.append("\u00A77◌");
                    } else {
                        h20.append("\u00A7b◯");
                    }
                }
                actionBar.append(h20);
            }
        } else if (hydrationBar != null) {
            hydrationBar.removeAll();
        }

        if (config.isEnabled(ConfigOption.TEMP_ENABLED)) {
            if (config.getRenderMethod(ConfigOption.TEMP_BAR_STYLE) == StatusRenderMethod.BOSSBAR) {
                if (tempBar == null) {
                    tempBar = Bukkit.createBossBar("Temp", BarColor.GREEN, BarStyle.SOLID);
                    tempBar.addPlayer(player);
                }
                TemperatureData tempData = plugin.getEngine().getTemperatureData();
                if (tempData.getMinBlockTemp() < 0) {
                    double padd = Math.abs(tempData.getMinBlockTemp());
                    tempBar.setProgress((temperature + padd) / (tempData.getMaxBlockTemp() + padd));
                } else {
                    tempBar.setProgress(temperature / tempData.getMaxBlockTemp());
                }

                String title = "Temp: \u00A7"
                        + ((temperature > 70 || temperature < -8)
                        && config.getBoolean(ConfigOption.TEMP_BAR_FLASH) ? (flicker.isEnabled() ? "c" : "f")
                        : temperature >= 30 ? "e" : temperature < 10 ? "f" : temperature < 0 ? "b" : "a") +
                        String.format("%.1f°", temperature);
                tempBar.setTitle(StringUtil.color(title));
                if (temperature > 100) tempBar.setColor(BarColor.RED);
                else tempBar.setColor(BarColor.GREEN);
            } else {
                if (tempBar != null) {
                    tempBar.removeAll();
                    tempBar = null;
                }
                String temp = "\u00A7f    Temp: \u00A7"
                        + ((temperature > 70 || temperature < -8)
                        && config.isEnabled(ConfigOption.TEMP_BAR_FLASH) ? (flicker.isEnabled() ? "c" : "f")
                        : temperature >= 30 ? "e" : temperature < 10 ? "f" : temperature < 0 ? "b" : "a") +
                        String.format("%.1f°", temperature);
                actionBar.append(temp);
            }
        } else if(tempBar != null) {
            tempBar.removeAll();
        }

        if (actionBar.length() > 0)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBar.toString()
                    + " \u00A76" + plugin.getEngine().getWorldProvider().getWorld(player.getWorld()).getChunksLoaded()
                    + " - " + String.format("%.1f°", tempExact) + " " + nulled));
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    /**
     * @return the hydration level of this player.
     */
    @Override
    public double getHydration() {
        return hydration;
    }

    /**
     * Set the hydration level of this player.
     *
     * @param hydration hydration of player. Bound between 0 and 20.
     */
    public void setHydration(float hydration) {
        this.hydration = hydration < 0 ? 0 : Math.min(hydration, MAX_THIRST);
    }

    @Override
    public void hydrate(float amount) {
        this.hydration += amount;
        if (hydration < 0) hydration = 0;
        if (hydration > 20) hydration = 20;
    }

    public void dehydrate(double amount) {
        this.hydration -= amount;
        if (hydration < 0) hydration = 0;
        if (hydration > 20) hydration = 20;

        Player player = Bukkit.getPlayer(id);
        if (player != null)
            player.getWorld().spawnParticle(Particle.WATER_SPLASH,
                    player.getEyeLocation(), 15, 0, 0, 0, 0);
    }

    /**
     * @param distance distance to add onto walking distance.
     * @param sprinting is the player currently sprinting.
     */
    public void addWalkDistance(float distance, boolean sprinting) {
        //if (isOnline() && Bukkit.getPlayer(id).getGameMode() == GameMode.CREATIVE) return;
        distanceWalked += sprinting ? distance + 0.3 : distance;
        if (distanceWalked >= distanceNextThirst) {
            distanceWalked = 0;
            distanceNextThirst = MathUtil.randomInt(dehydrateChance, dehydrateChance + dehydrateChanceRange);
            dehydrate(0.5);
        }
    }

    /**
     * Saves the players data.
     */
    public void save() {
        File file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
            config.set(id + ".temperature", temperature);
            config.set(id + ".temperature-to", tempExact);
            config.set(id + ".hydration", hydration);
            config.save(file);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * End the players session. This removes any boss bars from the player
     * and also saves all data to file.
     */
    public void endSession() {
        if (hydrationBar != null) hydrationBar.removeAll();
        if (tempBar != null) tempBar.removeAll();
        save();
    }

    /**
     * @return if the player is currently online.
     */
    public final boolean isOnline() {
        return Bukkit.getPlayer(id) != null;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof UserData && object.hashCode() == this.hashCode();
    }
}
