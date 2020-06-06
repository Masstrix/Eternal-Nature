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
import me.masstrix.eternalnature.core.temperature.*;
import me.masstrix.eternalnature.config.StatusRenderMethod;
import me.masstrix.eternalnature.config.SystemConfig;
import me.masstrix.eternalnature.core.world.TemperatureScanner;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.listeners.DeathListener;
import me.masstrix.eternalnature.util.*;
import me.masstrix.lang.langEngine.LanguageEngine;
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
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class UserData implements EternalUser {

    /**
     * Hard max value for hydration thirst.
     */
    public static final float MAX_THIRST = 20;

    private static final int dehydrateChance = 500;
    private static final int dehydrateChanceRange = 50;

    private SystemConfig config;
    private EternalNature plugin;
    private LanguageEngine le;

    private Stopwatch damageTimer = new Stopwatch();
    private Flicker flicker = new Flicker(300);
    private TemperatureScanner tempScanner;

    private BossBar hydrationBar, tempBar;
    private UUID id;
    private double temperature = 0, tempExact = 0;
    private double hydration = 20; // max is 20
    private float distanceWalked;
    private int distanceNextThirst;
    private int thirstTimer = 0;
    private long constantTick = -1;
    private boolean debugEnabled = false;
    private PlayerIdle playerIdle = new PlayerIdle();
    private Vector motion = new Vector();
    private boolean inMotion;
    private long lastMovementCheck = 0;

    public UserData(EternalNature plugin, UUID id) {
        this.id = id;
        this.plugin = plugin;
        this.le = plugin.getLanguageEngine();
        config = plugin.getSystemConfig();

        Player player = Bukkit.getPlayer(id);
        if (player != null) {
            WorldProvider provider = plugin.getEngine().getWorldProvider();
            WorldData data = provider.getWorld(player.getWorld());
            Location loc = player.getLocation();
            this.temperature = data.getAmbientTemperature(5, 15,
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    public UserData(EternalNature plugin, UUID id, double temperature, double hydration) {
        this.id = id;
        this.plugin = plugin;
        this.le = plugin.getLanguageEngine();
        this.temperature = temperature;
        this.hydration = hydration;
        config = plugin.getSystemConfig();
        distanceNextThirst = MathUtil.randomInt(dehydrateChance, dehydrateChance + dehydrateChanceRange);
    }

    /**
     * Sets the players cached motion.
     *
     * @param velocity current velocity of the player.
     */
    public void setMotion(Vector velocity) {
        this.motion = velocity;
        this.inMotion = velocity.length() > 0;
        lastMovementCheck = System.currentTimeMillis();
    }

    /**
     * Returns the motion vector of the player. This can be used
     * instead of {@code Player#getVecloty()} as it will return the
     * players true motion respecting any effects the player has and
     * if they are sprinting.
     *
     * @return the players motion vector.
     */
    @Override
    public Vector getMotion() {
        checkMotion();
        return motion;
    }

    /**
     * @return if the player is currently moving.
     */
    public boolean isInMotion() {
        checkMotion();
        return inMotion;
    }

    /**
     * Checks the players motion. If the player has not moved in 0.1 seconds
     * then there motion will be reset back to 0.
     */
    private void checkMotion() {
        if (inMotion && System.currentTimeMillis() - lastMovementCheck > 100) {
            inMotion = false;
            motion.zero();
        }
    }

    public UserData setThirstTimer(int time) {
        this.thirstTimer = time;
        return this;
    }

    /**
     * Performs a tick for the player. This will update all enabled settings for
     * the player and perform any related actions. This includes handling of
     * temperature scanning and updating, hydration changes and idle updating.
     */
    public final void tick() {
        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline() || player.isDead()) return;

        // Update the players idle state.
        playerIdle.check(player.getLocation());

        // Stop updating if the player is dead. This will stop players who
        // stay on the respawn screen for to long using resources.
        if (player.isDead()) {
            return;
        }

        // Create a region scanner if one has not been made already.
        if (tempScanner == null) {
            this.tempScanner = new TemperatureScanner(plugin, this, player);
            this.tempScanner.setScanScale(11, 5);
        }

        WorldProvider provider = plugin.getEngine().getWorldProvider();
        WorldData worldData = provider.getWorld(player.getWorld());
        Temperatures tempData = worldData.getTemperatures();

        // Updates the players temperature and applies damage to the player
        // if damage is enabled.
        if (config.isEnabled(ConfigOption.TEMPERATURE_ENABLED)) {
            // Update the players temperature
            updateTemperature(false);

            boolean damageEnabled = config.isEnabled(ConfigOption.TEMPERATURE_DMG);
            boolean isBurning = this.temperature >= tempData.getBurningPoint();
            boolean isFreezing = this.temperature <= tempData.getFreezingPoint();
            int damageDelay = config.getInt(ConfigOption.TEMPERATURE_DMG_DELAY);

            // Check if the player should be damaged and
            // damage them if is all true.
            if (damageEnabled && (isBurning || isFreezing) && damageTimer.hasPassed(damageDelay)) {
                damageTimer.startIfNew();
                damageTimer.start();
                double dmg = config.getDouble(ConfigOption.TEMPERATURE_DMG_AMOUNT);
                String msg = isBurning ? "death.heat" : "death.cold";
                damageCustom(player, dmg, le.getText(msg));
            }
        }

        // Updates the players hydration levels and applys damage id damage is enabled
        // for hydration.
        if (config.isEnabled(ConfigOption.HYDRATION_ENABLED)
                && player.getGameMode() != GameMode.CREATIVE) {
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
                    damageCustom(player, 1, le.getText("death.dehydrate"));
                }
            }
        }

        // Keeps a consistent 1s tick rate
        if (System.currentTimeMillis() - constantTick >= 1000) {
            // Count down the thirst timer
            if (thirstTimer > 0) {
                thirstTimer--;

                if (MathUtil.chance(6)) {
                    dehydrate(0.5);
                }
            }
            constantTick = System.currentTimeMillis();
        }
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
            WorldProvider provider = plugin.getEngine().getWorldProvider();
            WorldData worldData = provider.getWorld(player.getWorld());
            Temperatures temps = worldData.getTemperatures();
            TemperatureIcon icon = TemperatureIcon.getClosest(temperature, temps);

            String text = config.getString(ConfigOption.TEMPERATURE_TEXT);
            text = text.replaceAll("%temp_simple%", icon.getColor() + icon.getName() + "&f");
            text = text.replaceAll("%temp_icon%", icon.getColor() + icon.getIcon() + "&f");

            String tempInfoColor = icon.getColor().toString();

            if (config.isEnabled(ConfigOption.TEMPERATURE_BAR_FLASH)) {
                double burn = temps.getBurningPoint();
                double freeze = temps.getFreezingPoint();
                boolean flash = this.temperature <= freeze + 2 || this.temperature >= burn - 4;
                if (flash) {
                    tempInfoColor = flicker.isEnabled() ? "&c" : "&f";
                }
            }

            text = text.replaceAll("%temperature%", tempInfoColor + String.format("%.1f°", temperature) + "&f");

            // Append debug info for temperature
            if (debugEnabled) {
                text += " &d(exact: " + MathUtil.round(tempExact, 2) + ")";
            }

            if (config.getRenderMethod(ConfigOption.TEMPERATURE_BAR_STYLE) == StatusRenderMethod.BOSSBAR) {
                if (tempBar == null) {
                    tempBar = Bukkit.createBossBar("Loading...", BarColor.GREEN, BarStyle.SOLID);
                    tempBar.addPlayer(player);
                }
                // Update the bars progress
                double min = Math.abs(temps.getMinTemp());
                double max = temps.getMaxTemp() + min;
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
     * @return if the player is currently in idle.
     */
    @Override
    public boolean isIdle() {
        return playerIdle.isIdle();
    }

    @Override
    public PlayerIdle getPlayerIdleInfo() {
        return playerIdle;
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
     * @return if the players hydration is max.
     */
    public boolean isHydrationFull() {
        return hydration >= 19.5;
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
            config.set(id + ".effects.thirst", thirstTimer);
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
     * Resets the players temperature.
     */
    public void resetTemperature() {
        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline()) return;

        WorldProvider provider = plugin.getEngine().getWorldProvider();
        WorldData worldData = provider.getWorld(player.getWorld());

        // Stop if world is invalid.
        if (worldData == null) return;

        // Reset the players temperature
        Location loc = player.getLocation();
        tempExact = worldData.getAmbientTemperature(5, 15,
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        this.temperature = tempExact;
    }

    /**
     * Update the players temperature. This will not work if the player is offline or
     * they are currently dead.
     *
     * @param forceNew if true this update will instantly become the
     *                 players temperature.
     */
    public void updateTemperature(boolean forceNew) {
        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline() || player.isDead()) return;
        if (!config.isEnabled(ConfigOption.TEMPERATURE_ENABLED)) return;

        WorldProvider provider = plugin.getEngine().getWorldProvider();
        WorldData worldData = provider.getWorld(player.getWorld());

        // Stop if world is invalid.
        if (worldData == null) return;

        Location loc = player.getLocation();
        Temperatures tempData = worldData.getTemperatures();

        // Handle temperature ticking.
        boolean inWater = isBlockWater(loc.getBlock());
        double emission = 0;

        // Add nearby block temperature if enabled.
        if (config.isEnabled(ConfigOption.TEMPERATURE_USE_BLOCKS)) {
            if (forceNew) tempScanner.quickUpdate();
            else tempScanner.tick();
            emission += tempScanner.getTemperatureEmission();
        }

        // Add average nearby biome temperature if enabled
        if (config.isEnabled(ConfigOption.TEMPERATURE_USE_BIOMES)) {
            int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
            emission += worldData.getBlockAmbientTemperature(x, y, z);
        }

        // Add environmental modifiers if enabled.
        if (config.isEnabled(ConfigOption.TEMPERATURE_USE_ENVIRO)) {
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

            // If the world has a height gradient add it to the temperature
            HeightGradient gradient = HeightGradient.getGradient(loc.getWorld().getEnvironment());
            if (gradient != null) {
                emission += gradient.getModifier(loc.getBlockY());
            }
        }

        // Add item based temperatures if enabled.
        if (config.isEnabled(ConfigOption.TEMPERATURE_USE_ITEMS)) {
            // Add armor to temp.
            ItemStack[] armor = player.getEquipment().getArmorContents();
            for (ItemStack i : armor) {
                if (i == null) continue;
                emission += tempData.getEmission(i.getType(), TempModifierType.CLOTHING);
            }

            // Add temperature depending on what the player is holding
            Material mainHand = player.getInventory().getItemInMainHand().getType();
            Material offHand = player.getInventory().getItemInOffHand().getType();
            if (mainHand != Material.AIR) {
                double mainTemp = tempData.getEmission(mainHand, TempModifierType.BLOCK);
                emission += mainTemp / 10;
            }
            if (offHand != Material.AIR) {
                double offTemp = tempData.getEmission(mainHand, TempModifierType.BLOCK);
                emission += offTemp / 10;
            }
        }

        if (!Double.isInfinite(emission) && !Double.isNaN(emission)) {
            this.tempExact = emission;
            tempData.updateMinMaxTempCache(tempExact);
        }

        // Force the new exact temperature on the player.
        if (forceNew) {
            temperature = tempExact;
            return;
        }

        // Reset the players temperature to the exact temp if it is invalid.
        temperature = MathUtil.fix(temperature, tempExact);

        // Push the
        progressTemperature(inWater);
    }

    /**
     * Gradually updates the temperature to get to the exact temperature.
     *
     * @param inWater is the player currently in water.
     */
    private void progressTemperature(boolean inWater) {
        // Stop updating temperature if is already exactly the same as
        // the expected value.
        if (temperature == tempExact) return;
        if (!config.isEnabled(ConfigOption.TEMPERATURE_ENABLED)) return;

        // Change players temperature gradually
        double diff = MathUtil.diff(this.tempExact, this.temperature);
        if (diff > 0.09) {
            int division = 30;
            if (inWater && tempExact < temperature) {
                division = 10;
            }
            int maxDelta = config.getInt(ConfigOption.TEMPERATURE_MAX_DELTA);
            double delta = diff / division;
            if (delta > maxDelta) delta = maxDelta;
            if (this.tempExact > this.temperature) this.temperature += delta;
            else this.temperature -= delta;
        } else if (diff < 0.1) {
            // stop trying to round temperature.
            temperature = tempExact;
        }
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
