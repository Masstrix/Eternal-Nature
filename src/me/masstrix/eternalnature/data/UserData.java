/*
 * Copyright 2019 Matthew Denton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.masstrix.eternalnature.data;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.api.EternalUser;
import me.masstrix.eternalnature.config.ConfigOption;
import me.masstrix.eternalnature.core.HeightGradient;
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.config.StatusRenderMethod;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.listeners.DeathListener;
import me.masstrix.eternalnature.util.*;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    private int thirstTimer = 0;
    private long constantTick = 0;
    private boolean debugEnabled = false;

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

    public UserData(EternalNature plugin, UUID id, float temperature, float hydration) {
        this.id = id;
        this.plugin = plugin;
        this.temperature = temperature;
        this.hydration = hydration;
        config = plugin.getSystemConfig();
        distanceNextThirst = MathUtil.randomInt(dehydrateChance, dehydrateChance + dehydrateChanceRange);
    }

    /**
     * Resets the players temperature to the exact temperature to where they currently are.
     */
    public void resetTemperature() {
        tick();
        this.temperature = tempExact;
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
        TemperatureData tempData = plugin.getEngine().getTemperatureData();

        // Handle temperature ticking.
        if (data != null && config.isEnabled(ConfigOption.TEMPERATURE_ENABLED)) {
            boolean inWater = isBlockWater(loc.getBlock());
            float emission = 0;
            emission += data.getBlockTemperature(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

            // If the world has a height gradient add it to the temperature
            HeightGradient gradient = HeightGradient.getGradient(loc.getWorld().getEnvironment());
            if (gradient != null) {
                emission += gradient.getModifier(loc.getBlockY());
            }

            // If the player is swimming, subtract temperature from depth.
            if (inWater) {
                int height = 0;
                for (int i = 1; i < 30; i++) {
                    if (!isBlockWater(loc.getBlock().getRelative(0, i, 0))) break;
                    height++;
                }
                double depthSub = (double) height / 30D * 4;
                emission -= depthSub;
            }

            // Add armor to temp.
            ItemStack[] armor = player.getEquipment().getArmorContents();
            for (ItemStack i : armor) {
                if (i == null) continue;
                emission += tempData.getArmorModifier(i.getType());
            }

            if (!Float.isInfinite(emission) && !Float.isNaN(emission)) {
                this.tempExact = emission;
                tempData.updateMinMaxTempCache(tempExact);
            }

            // Reset the players temperature to the exact temp if it is invalid.
            temperature = MathUtil.fix(temperature, tempExact);

            // Change players temperature gradually
            float diff = MathUtil.diff(this.tempExact, this.temperature);
            if (diff > 0.09) {
                int division = 30;
                if (inWater && tempExact < temperature) {
                    division = 10;
                }
                float toAdd = diff / division;
                if (this.tempExact > this.temperature) this.temperature += toAdd;
                else this.temperature -= toAdd;
            } else if (diff < 0.1) {
                temperature = tempExact;
            }

            // Damage the player if they are to hot or cold or are dehydrated.
            if (this.temperature >= tempData.getBurningPoint()
                    && config.isEnabled(ConfigOption.TEMPERATURE_BURN)) {
                damageTimer.startIfNew();
                if (damageTimer.hasPassed((long) (3000 - (temperature * 2)))) {
                    damageTimer.start();
                    damageCustom(player, 1, config.getString(ConfigOption.MSG_DEATH_HEAT));
                }
            } else if (this.temperature <= tempData.getFreezingPoint()
                    && config.isEnabled(ConfigOption.TEMPERATURE_FREEZE)) {
                player.removePotionEffect(PotionEffectType.SLOW);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 120,
                        1, true, false, true));
                damageTimer.startIfNew();
                if (damageTimer.hasPassed(3000)) {
                    damageTimer.start();
                    damageCustom(player, 1, config.getString(ConfigOption.MSG_DEATH_COLD));
                }
            }
        }

        // Handle hydration ticking.
        if (config.isEnabled(ConfigOption.HYDRATION_ENABLED) && player.getGameMode() != GameMode.CREATIVE) {
            // Sweat randomly. Becomes more common the warmer you are.
            if (plugin.getSystemConfig().isEnabled(ConfigOption.TEMPERATURE_SWEAT)) {
                if (MathUtil.randomInt((int) (1000 * 1.5)) <= temperature * (temperature * 0.005)) {
                    dehydrate(0.25);
                }
            }

            // Handle damage tick if player is dehydrated or to hot/cold
            if (hydration <= 0 && config.isEnabled(ConfigOption.HYDRATION_DAMAGE)) {
                damageTimer.startIfNew();
                if (damageTimer.hasPassed(3000)) {
                    damageTimer.start();
                    damageCustom(player, 1, config.getString(ConfigOption.MSG_DEATH_WATER));
                }
            }
        }

        // Keeps a consistent 1s tick rate
        if (constantTick == 0 || constantTick - System.currentTimeMillis() >= 1000) {
            // Count down the thirst timer
            if (thirstTimer > 0) {
                thirstTimer--;
            }
        }

        if (!config.isEnabled(ConfigOption.TEMPERATURE_ENABLED) || !debugEnabled) return;
        World world = player.getWorld();
        final double MAX = tempData.getMaxBiomeTemp();
        final double MIN = tempData.getMinBiomeTemp();
        /*
        * Scans around the player and projects the temperature above blocks.
        * */
        new CuboidScanner(4, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                (CuboidScanner.CuboidTask) (x, y, z) -> {
            Block block = world.getBlockAt(x, y, z);
            if (!block.isPassable() && block.getType().isSolid() || block.getType() == Material.TORCH) {

                double temp = provider.getWorld(world).getBlockTemperature(x, y, z);
                java.awt.Color color;

                if (temp != Float.NEGATIVE_INFINITY) {
                    float[] hsbVals = new float[3];
                    java.awt.Color.RGBtoHSB(126, 255, 0, hsbVals);

                    float colorRotate;
                    if (temp > 0) {
                        colorRotate = (float) (temp / MAX);
                        // Shift the hue clockwise by 25% (from green to red).
                        color = new java.awt.Color(java.awt.Color.HSBtoRGB(0.25f - (0.25f * colorRotate),
                                hsbVals[1], hsbVals[2]));
                    } else {
                        colorRotate = (float) (Math.abs(temp) / MIN);
                        // Shift the hue counter clockwise by 30% (from green to blue).
                        color = new java.awt.Color(java.awt.Color.HSBtoRGB(0.30f * colorRotate,
                                hsbVals[1], hsbVals[2]));
                    }
                } else {
                    color = new java.awt.Color(255, 0, 206); // Invalid color
                }

                player.spawnParticle(Particle.REDSTONE, block.getLocation().add(0.5, 1.2, 0.5),
                        1, 0, 0, 0, new Particle.DustOptions(org.bukkit.Color.fromRGB(
                                color.getRed(), color.getGreen(), color.getBlue()), 1));
            }
        }).start();
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
                    BarColor color = isThirsty() ? BarColor.GREEN : BarColor.BLUE;
                    hydrationBar = Bukkit.createBossBar("h2-", color, BarStyle.SEGMENTED_12);
                    hydrationBar.addPlayer(player);
                }
                hydrationBar.setProgress(Math.abs(hydration / 20));
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
                char bubble = '\u2B58'; // Unicode 11096
                for (int i = 0; i < 10; i++) {
                    if (i < mid) {
                        if (isThirsty()) h20.append(ChatColor.GREEN);
                        else h20.append(ChatColor.AQUA);
                    } else if (i > mid) {
                        h20.append("\u00A78");
                    } else {
                        if (isThirsty()) h20.append(ChatColor.DARK_GREEN);
                        else h20.append(ChatColor.DARK_AQUA);
                    }
                    h20.append(bubble);
                }
                actionBar.append(h20);
            }
        } else if (hydrationBar != null) {
            hydrationBar.removeAll();
        }

        if (config.isEnabled(ConfigOption.TEMPERATURE_ENABLED)) {
            TemperatureData tempData = plugin.getEngine().getTemperatureData();
            TemperatureData.TemperatureIcon icon = tempData.getClosestIconName(temperature);

            String text = config.getString(ConfigOption.TEMPERATURE_TEXT);
            text = text.replaceAll("%temp_simple%", icon.getColor() + icon.getName() + "&f");
            text = text.replaceAll("%temp_icon%", icon.getColor() + icon.getIcon() + "&f");

            String tempInfoColor = icon.getColor().toString();

            if (config.isEnabled(ConfigOption.TEMPERATURE_BAR_FLASH)) {
                int burn = tempData.getBurningPoint();
                int freeze = tempData.getFreezingPoint();
                boolean flash = this.temperature <= freeze + 2 || this.temperature >= burn - 4;
                if (flash) {
                    tempInfoColor = flicker.isEnabled() ? "&c" : "&f";
                }
            }

            text = text.replaceAll("%temperature%", tempInfoColor + String.format("%.1f°", temperature) + "&f");

            if (config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE) == StatusRenderMethod.BOSSBAR) {
                if (tempBar == null) {
                    tempBar = Bukkit.createBossBar("Loading...", BarColor.GREEN, BarStyle.SOLID);
                    tempBar.addPlayer(player);
                }
                // Update the bars progress
                double min = Math.abs(tempData.getMinTemp());
                double max = tempData.getMaxTemp() + min;
                double temp = this.temperature + min;
                double progress = temp / max;
                if (progress >= 0D && progress <= 1)
                    tempBar.setProgress(progress);

                // Set its color and title
                tempBar.setTitle(StringUtil.color(text));
                tempBar.setColor(BossBarUtil.fromBukkitColor(icon.getColor()));
            } else {
                if (tempBar != null) {
                    tempBar.removeAll();
                    tempBar = null;
                }
                if (actionBar.length() > 0)
                    actionBar.append("    ");
                actionBar.append(StringUtil.color("&f" + text));
            }
        } else if(tempBar != null) {
            tempBar.removeAll();
        }

        if (actionBar.length() > 0)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(actionBar.toString()));
    }

    /**
     * Sets if debug mode is enabled for this player. When debug mode is enabled
     * the local temperatures will be displayed around them along with extra info.
     *
     * @param enabled should debug mode be enabled.
     * @return the state of debug mode.
     */
    public boolean setDebug(boolean enabled) {
        this.debugEnabled = enabled;
        return debugEnabled;
    }

    /**
     * @return if debug mode is enabled.
     */
    public boolean isDebugEnabled() {
        return debugEnabled;
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

    /**
     * @return if the user is currently thirsty.
     */
    public boolean isThirsty() {
        return thirstTimer > 0;
    }

    /**
     * Adds time to the players thirst in seconds.
     *
     * @param sec time in seconds on how long to add to the players
     *            thirst effect.
     */
    public void addThirst(int sec) {
        thirstTimer += sec;
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
        if (isOnline() && Bukkit.getPlayer(id).getGameMode() == GameMode.CREATIVE) return;
        if (!config.isEnabled(ConfigOption.HYDRATION_ENABLED)) return;
        distanceWalked += sprinting ? distance + 0.3 : distance;
        if (distanceWalked >= distanceNextThirst) {
            distanceWalked = 0;
            distanceNextThirst = MathUtil.randomInt(dehydrateChance, dehydrateChance + dehydrateChanceRange);
            dehydrate(0.5);
        }
    }

    /**
     * Returns if a block is representable as water. If a block is waterlogged or
     * is a block that always has water in it such as kelp and sea grass, true
     * will be returned.
     *
     * @param block block to check if it is "water".
     * @return if the player is in water.
     */
    private boolean isBlockWater(Block block) {
        switch (block.getType()) {
            case WATER:
            case SEAGRASS:
            case TALL_SEAGRASS:
            case KELP_PLANT:
                return true;
        }
        return block.getBlockData() instanceof Waterlogged
                && ((((Waterlogged) block.getBlockData()).isWaterlogged()));
    }

    /**
     * Damages the player for an amount and applies a custom death message if the
     * damage results in them dying.
     *
     * @param player player to damage.
     * @param amount amount to damage for.
     * @param deathMsg custom death message. All %name% is replaced for their name.
     */
    private void damageCustom(Player player, double amount, String deathMsg) {
        if (player.getHealth() - amount <= 0) {
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, amount));
            DeathListener.logCustomReason(player, deathMsg.replaceAll("%name%", player.getDisplayName()));
            player.setHealth(0.0);
        } else {
            player.damage(amount);
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, amount));
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
            config.set(id + ".temp", temperature);
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
