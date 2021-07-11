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

package me.masstrix.eternalnature.player;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.PluginData;
import me.masstrix.eternalnature.api.EternalUser;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.HeightGradient;
import me.masstrix.eternalnature.core.temperature.TempModifierType;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.temperature.TemperatureScanner;
import me.masstrix.eternalnature.core.temperature.TemperatureUnit;
import me.masstrix.eternalnature.core.world.WeatherType;
import me.masstrix.eternalnature.core.world.WorldData;
import me.masstrix.eternalnature.core.world.WorldProvider;
import me.masstrix.eternalnature.core.world.wind.Wind;
import me.masstrix.eternalnature.listeners.DeathListener;
import me.masstrix.eternalnature.util.Direction;
import me.masstrix.eternalnature.util.MathUtil;
import me.masstrix.eternalnature.util.Stopwatch;
import me.masstrix.eternalnature.util.StringUtil;
import me.masstrix.lang.langEngine.LanguageEngine;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserData implements EternalUser, Configurable {

    /**
     * Hard max value for hydration thirst.
     */
    public static final float MAX_THIRST = 20;
    private static final int dehydrateChance = 500;
    private static final int dehydrateChanceRange = 50;

    private final EternalNature PLUGIN;
    private final LanguageEngine LANG;

    private final DebugOptions DEBUG_OPTIONS = new DebugOptions();
    private Stopwatch damageTimer = new Stopwatch();
    private TemperatureScanner tempScanner;
    final Actionbar ACTIONBAR;
    private Set<StatRenderer> statRenderers = new HashSet<>();
    private TemperatureUnit viewUnit = TemperatureUnit.CELSIUS;

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
    private boolean isBurning;
    private boolean isFreezing;

    //
    // Configuration
    //

    // Hydration settings
    private boolean isHydEnabled;
    private boolean doHydDamage;
    private double hydDamageAmount;
    private int hydDamageDelay;
    private boolean hydSweat;
    private boolean isThirstEnabled;
    private double thirstAmount;

    // Temperature settings
    private boolean isTmpEnabled;
    private boolean doTmpDamage;
    private double tmpDamageAmount;
    private int tmpDamageDelay;
    private int tmpMaxDelta;
    private int scanArea;
    private int scanHeight;

    private boolean tmpUseBlocks;
    private boolean tmpUseBiomes;
    private boolean tmpUseWeather;
    private boolean tmpUseItems;
    private boolean tmpUseEnvironment;

    public UserData(EternalNature plugin, UUID id) {
        this.id = id;
        this.PLUGIN = plugin;
        this.LANG = plugin.getLanguageEngine();
        this.ACTIONBAR = new Actionbar(Bukkit.getPlayer(id));

        Player player = Bukkit.getPlayer(id);
        String playerName = null;
        if (player != null) {
            playerName = player.getName();
            WorldProvider provider = plugin.getEngine().getWorldProvider();
            WorldData data = provider.getWorld(player.getWorld());
            Location loc = player.getLocation();
            this.temperature = data.getAmbientTemperature(5, 15,
                    loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
        setup();

        // debugging
        plugin.getDebugLogger().info("Created user " + id + (playerName != null ? " (" + playerName + ")" : ""));
    }

    public UserData(EternalNature plugin, UUID id, double temperature, double hydration) {
        this.id = id;
        this.PLUGIN = plugin;
        this.LANG = plugin.getLanguageEngine();
        this.temperature = temperature;
        this.hydration = hydration;
        this.ACTIONBAR = new Actionbar(Bukkit.getPlayer(id));
        distanceNextThirst = MathUtil.randomInt(dehydrateChance, dehydrateChance + dehydrateChanceRange);
        setup();

        // debugging
        Player player = Bukkit.getPlayer(id);
        String playerName = player != null ? player.getName() : null;
        plugin.getDebugLogger().info("Created user " + id + (playerName != null ? " (" + playerName + ")" : ""));
    }

    /**
     * Setups the common objects for the player.
     */
    private void setup() {
        Player player = Bukkit.getPlayer(id);
        statRenderers.add(new HydrationRenderer(player, this));
        statRenderers.add(new TemperatureRender(player, this));
        statRenderers.forEach(ACTIONBAR::add);

        // Loads debug options
        YamlConfiguration playerConfig = PLUGIN.getPlayerConfig().getYml();
        DEBUG_OPTIONS.loadFromConfig(playerConfig.getConfigurationSection(id.toString()));

        // Reload all the stat renderers as well.
        Configuration config = PLUGIN.getRootConfig();
        statRenderers.forEach(config::reload);

        // Update all the config options
        config.reload(this);
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        // hydration
        isHydEnabled = section.getBoolean("hydration.enabled", true);
        doHydDamage = section.getBoolean("hydration.damage.enabled", true);
        hydDamageAmount = section.getDouble("hydration.damage.amount", 0.5);
        hydDamageDelay = section.getInt("hydration.damage.delay", 2000);
        hydSweat = section.getBoolean("hydration.sweat", true);
        isThirstEnabled = section.getBoolean("hydration.thirst-effect.enabled", true);
        thirstAmount = section.getDouble("hydration.thirst-effect.amount", 0.1);

        // temp
        isTmpEnabled = section.getBoolean("temperature.enabled", true);
        doTmpDamage = section.getBoolean("temperature.damage.enabled", true);
        tmpDamageAmount = section.getDouble("temperature.damage.amount", 1);
        tmpDamageDelay = section.getInt("temperature.damage.delay", 1000);
        tmpMaxDelta = section.getInt("temperature.max-delta-chance", 15);

        tmpUseBlocks = section.getBoolean("temperature.scanning.use-blocks", true);
        tmpUseBiomes = section.getBoolean("temperature.scanning.use-biomes", true);
        tmpUseItems = section.getBoolean("temperature.scanning.use-weather", true);
        tmpUseWeather = section.getBoolean("temperature.scanning.use-items", true);
        tmpUseEnvironment = section.getBoolean("temperature.scanning.use-environment", true);

        scanArea = section.getInt("temperature.scanning.advanced.area", 11);
        scanHeight = section.getInt("temperature.scanning.advanced.height", 5);

        if (tempScanner != null)
            tempScanner.setScanScale(scanArea, scanHeight);

        // Reload all the stat renderers as well.
        Configuration config = PLUGIN.getRootConfig();
        statRenderers.forEach(config::reload);

        // Reload temperature scanner settings.
        if (tempScanner != null) config.reload(tempScanner);
    }

    /**
     * Returns the debug options for this player.
     *
     * @return the debug options for this player.
     */
    public DebugOptions getDebugOptions() {
        return DEBUG_OPTIONS;
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
        // Disables player afk idling if it's disabled by debug mode
        if (debugEnabled && DEBUG_OPTIONS.isEnabled(DebugOptions.Type.DISABLE_AFK))
            playerIdle.reset();

        // Stop updating if the player is dead. This will stop players who
        // stay on the respawn screen for to long using resources.
        if (player.isDead()) {
            return;
        }

        // Updates the players temperature and applies damage to the player
        // if damage is enabled.
        if (isTmpEnabled) {
            // Create a region scanner if one has not been made already.
            if (tempScanner == null) {
                this.tempScanner = new TemperatureScanner(PLUGIN, this, player);
                this.tempScanner.setScanScale(scanArea, scanHeight);
            }

            WorldProvider provider = PLUGIN.getEngine().getWorldProvider();
            WorldData worldData = provider.getWorld(player.getWorld());
            TemperatureProfile tempProfile = worldData.getTemperatures();
            this.tempScanner.useTemperatureProfile(tempProfile);

            // Update the players temperature
            updateTemperature(false);

            isBurning = this.temperature >= tempProfile.getBurningPoint();
            isFreezing = this.temperature <= tempProfile.getFreezingPoint();

            // Check if the player should be damaged and
            // damage them if is all true.
            if (doTmpDamage && (isBurning || isFreezing) && damageTimer.hasPassed(tmpDamageDelay)) {
                damageTimer.startIfNew();
                damageTimer.start();
                String msg = isBurning ? "death.heat" : "death.cold";
                damageCustom(player, tmpDamageAmount, LANG.getText(msg));
            }
        } else {
            isBurning = false;
            isFreezing = false;
        }

        // Updates the players hydration levels and applys damage id damage is enabled
        // for hydration.
        if (isHydEnabled
                && player.getGameMode() != GameMode.CREATIVE
                && player.getGameMode() != GameMode.SPECTATOR) {
            // Sweat randomly. Becomes more common the warmer you are.
            if (hydSweat) {
                if (MathUtil.randomInt(1500) <= temperature * (temperature * 0.005)) {
                    dehydrate(0.25);
                }
            }

            // Handle damage tick if player is dehydrated or to hot/cold
            if (doHydDamage && hydration <= 0) {
                damageTimer.startIfNew();
                if (damageTimer.hasPassed(hydDamageDelay)) {
                    damageTimer.start();
                    damageCustom(player, hydDamageAmount, LANG.getText("death.dehydrate"));
                }
            }
        }

        // Quickly start dehydrating the player when the thirst effect is applied.
        if (isThirstEnabled && thirstTimer > 0 && MathUtil.chance(2)) {
            dehydrate(thirstAmount);
        }

        // Keeps a constant tick rate of 1 second.
        if (System.currentTimeMillis() - constantTick >= 1000) {
            // Count down the thirst timer
            if (thirstTimer > 0) {
                thirstTimer--;
            }
            constantTick = System.currentTimeMillis();
        }

        // Extra debug info to be displayed to the player when debug mode is enabled.
        if (debugEnabled && DEBUG_OPTIONS.isEnabled(DebugOptions.Type.LOG_OUTPUT)) {
            ComponentBuilder builder = new ComponentBuilder();

            builder.append("\n————————————————\n").color(ChatColor.GRAY);
            builder.append("  Eternal Nature Debug Info\n").color(PluginData.Colors.PRIMARY);

            builder.append("").color(ChatColor.GRAY).italic(true);
            if (!isTmpEnabled) builder.append("\n • Temperature is disabled.");
            if (!isHydEnabled) builder.append("\n • Hydration is disabled.");
            if (!isThirstEnabled) builder.append("\n • Thirst is disabled.\n");

            // Temperature debug info
            if (isTmpEnabled) {
                builder.append("\nTemperature: ", ComponentBuilder.FormatRetention.NONE)
                        .color(PluginData.Colors.MESSAGE)
                        .append("" + MathUtil.round(temperature, 2))
                        .color(PluginData.Colors.SECONDARY)
                        .append("/" + MathUtil.round(tempExact, 2))
                        .color(PluginData.Colors.TERTIARY);
                if (isBurning) builder.append(" ☀").color(ChatColor.RED)
                        .append("(DMG)").color(ChatColor.GRAY);
                if (isFreezing) builder.append(" ※").color(ChatColor.AQUA)
                        .append("(DMG)").color(ChatColor.GRAY);
            }

            // Hydration debug info
            if (isHydEnabled) {
                builder.append("\nHydration: ", ComponentBuilder.FormatRetention.NONE)
                        .color(PluginData.Colors.MESSAGE)
                        .append("" + MathUtil.round(hydration, 2))
                        .color(PluginData.Colors.SECONDARY);
            }

            if (isThirstEnabled) {
                builder.append("\nThirst: ", ComponentBuilder.FormatRetention.NONE)
                        .color(PluginData.Colors.MESSAGE)
                        .append("" + thirstAmount + " | " + thirstTimer)
                        .color(PluginData.Colors.SECONDARY);
            }

            // Wind
            Wind wind = getWorld().getWind();
            Location loc = player.getLocation();
            Vector windDirection = wind.getDirection(loc.getX(), loc.getZ());
            double windSpeed = wind.getWindSpeed(loc.getX(), loc.getY(), loc.getZ());
            Direction cardinol = Direction.compass(windDirection.getX(), windDirection.getZ());

            builder.append("\n\nWind Speed: ", ComponentBuilder.FormatRetention.NONE)
                    .color(PluginData.Colors.MESSAGE)
                    .append(String.format("%.3f", windSpeed))
                    .color(PluginData.Colors.SECONDARY);
            builder.append("\nWind Direction: ", ComponentBuilder.FormatRetention.NONE)
                    .color(PluginData.Colors.MESSAGE)
                    .append(String.format("%.3f (%s)",
                            Math.toDegrees(Math.atan2(windDirection.getX(), windDirection.getZ())),
                            cardinol.getShortHand()))
                    .color(PluginData.Colors.SECONDARY);
            builder.append("\nWind Gust: ", ComponentBuilder.FormatRetention.NONE)
                    .color(PluginData.Colors.MESSAGE)
                    .append(String.format("%.3f (%s)", wind.getGustStrength(), wind.isGusty()))
                    .color(PluginData.Colors.SECONDARY);

            builder.append("\n\n");
            builder.append("Click Here").color(PluginData.Colors.ACTION)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("\u00A7eClick to disable Mode.")))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/en debug"));
            builder.append(" to disable debug mode.", ComponentBuilder.FormatRetention.NONE)
                    .color(PluginData.Colors.MESSAGE);
            builder.append("\n");
            builder.append("Click Here").color(PluginData.Colors.ACTION)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("\u00A7eClick to disable Messages.")))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/en debug set " + DebugOptions.Type.LOG_OUTPUT + " false"));
            builder.append(" to disable these messages.", ComponentBuilder.FormatRetention.NONE).color(PluginData.Colors.MESSAGE);

            builder.append("\n————————————————").color(ChatColor.GRAY);

            player.spigot().sendMessage(builder.create());
        }

        // Run triggers for this player.
        PLUGIN.getTriggerManager().attemptToTrigger(this, player);

        //foo();
    }

    private void foo() {
        WorldData data = getWorld();
        Wind wind = data.getWind();
        Player player = Bukkit.getPlayer(getUniqueId());
        Location loc = player.getLocation();

        int vision = 60;
        int linesCount = 10;

        String[] lines = new String[linesCount];
        double[] speeds = new double[vision];
        boolean[][] isGreen = new boolean[linesCount][vision];

        double max = 1;
        int center = vision / 2;

        for (int i = 0; i < vision; i++) {
            double add = wind.TICK_PROGRESS * 80;
            double offset = (add * i) - (add * center);
            double windSpeed = wind.getWindSpeed(loc.getX(), loc.getY(), loc.getZ(), offset);
            if (windSpeed > max) max = windSpeed;
            speeds[i] = windSpeed;
        }

        String txt = "*";
        String base = txt.repeat(vision);

        for (int i = 0; i < linesCount; i++) {
            lines[i] = base;
        }

        // Construct lines
        for (int i = 0; i < vision; i++) {
            double speed = speeds[i];
            double toMax = speed / max;

            int line = Math.max(0, (int) Math.floor(linesCount * toMax) - 1);
            isGreen[line][i] = true;
        }

        for (int i = 0; i < linesCount; i++) {
            boolean[] toggles = isGreen[i];
            StringBuilder builder = new StringBuilder();

            int index = 0;
            for (boolean toggle : toggles) {
                if (index == center) {
                    builder.append(toggle ? "&b" : "&f");
                } else {
                    builder.append(toggle ? "&a" : "&7");
                }
                builder.append("█");
                index++;
            }

            lines[(linesCount - i) - 1] = builder.toString();
        }

        StringBuilder combined = new StringBuilder();
        for (String s : lines) {
            combined.append("&7").append(s).append("\n");
        }

        player.setPlayerListFooter(StringUtil.color(combined.toString()));
    }

    /**
     * Finds and returns a stat renderer by class type. This will look through
     * all stat renderers and find the first matching renderer to the class.
     *
     * @param clazz class to get.
     * @return a stat renderer or null if non exists for the class.
     */
    public ActionbarItem getStatRenderer(Class<? extends ActionbarItem> clazz) {
        for (ActionbarItem a : statRenderers) {
            if (a.getClass().equals(clazz) || clazz.isAssignableFrom(a.getClass()))
                return a;
        }
        return null;
    }

    /**
     * Called by {@link me.masstrix.eternalnature.core.Renderer} to render all components
     * for the players perspective.
     */
    public final void render() {
        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline()) return;
        statRenderers.forEach(StatRenderer::render);
        ACTIONBAR.send();
    }

    /**
     * @return the users unique id.
     */
    public UUID getUniqueId() {
        return id;
    }

    /**
     * Returns the temperature unit the player prefers to see temperatures displayed in.
     *
     * @return the unit this player will see in.
     */
    public TemperatureUnit getViewUnit() {
        return viewUnit;
    }

    /**
     * Sets the unit used to display temperatures to the player in. Once set
     * every temnperature will be converted to this unit for this player.
     *
     * @param unit unit for the player to see temperatures in.
     */
    public void setViewUnit(TemperatureUnit unit) {
        this.viewUnit = unit;
    }

    /**
     * Returns the {@link WorldData} for the world the player is currently in.
     *
     * @return the world data that the player is currently in.
     */
    public WorldData getWorld() {
        WorldProvider provider = PLUGIN.getEngine().getWorldProvider();
        Player p = Bukkit.getPlayer(id);
        if (p == null) return provider.getFirstWorld();
        return provider.getWorld(p.getWorld());
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

    protected double getTemperatureExact() {
        return tempExact;
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
    @Override
    public boolean isThirsty() {
        return thirstTimer > 0;
    }

    @Override
    public int getThirstTime() {
        return thirstTimer;
    }

    /**
     * Adds time to the players thirst in seconds.
     *
     * @param sec time in seconds on how long to add to the players
     *            thirst effect.
     */
    @Override
    public void addThirst(int sec) {
        thirstTimer += sec;
        if (thirstTimer < 0) thirstTimer = 0;
    }

    @Override
    public void hydrate(float amount) {
        this.hydration += amount;
        if (hydration < 0) hydration = 0;
        if (hydration > 20) hydration = 20;
    }

    public void dehydrate(double amount) {
        boolean showParticles = this.hydration > 0;
        this.hydration -= amount;
        if (hydration < 0) hydration = 0;
        if (hydration > 20) hydration = 20;

        Player player = Bukkit.getPlayer(id);
        if (player != null && showParticles)
            player.getWorld().spawnParticle(Particle.WATER_SPLASH,
                    player.getEyeLocation(), 15, 0, 0, 0, 0);
    }

    /**
     * @param distance distance to add onto walking distance.
     * @param sprinting is the player currently sprinting.
     */
    public void addWalkDistance(float distance, boolean sprinting) {
        if (isOnline() && Bukkit.getPlayer(id).getGameMode() == GameMode.CREATIVE) return;
        if (!isHydEnabled) return;
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
            PLUGIN.getDebugLogger().info(String.format("%1$s was killed for %2$f (message: %3$s",
                    player.getName(), amount, deathMsg));
        } else {
            player.damage(amount);
            player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.CUSTOM, amount));
            PLUGIN.getDebugLogger().info(String.format("%1$s was damaged for %2$f (message: %3$s",
                    player.getName(), amount, deathMsg));
        }
    }

    /**
     * Saves the players data.
     */
    public void save() {
        Configuration playerConfig = PLUGIN.getPlayerConfig();
        YamlConfiguration config = playerConfig.getYml();
        config.set(id + ".temp", temperature);
        config.set(id + ".hydration", hydration);
        config.set(id + ".effects.thirst", thirstTimer);
        config.set(id + ".unit", viewUnit.name());
        DEBUG_OPTIONS.saveToConfig(config.getConfigurationSection(id.toString()));
        playerConfig.save();
    }

    /**
     * End the players session. This removes any boss bars from the player
     * and also saves all data to file.
     */
    public void endSession() {
        statRenderers.forEach(StatRenderer::reset);
        save();
    }

    /**
     * Resets the players temperature.
     */
    public void resetTemperature() {
        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline()) return;

        WorldProvider provider = PLUGIN.getEngine().getWorldProvider();
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
     * Returns if the player is hot enough to be getting burnt.
     *
     * @return if the player is burning.
     */
    public boolean isPlayerBurning() {
        return isBurning;
    }

    /**
     * Returns if the player is cold enough to be getting frozen.
     *
     * @return if the player is freezing.
     */
    public boolean isPlayerFreezing() {
        return isFreezing;
    }

    /**
     * Update the players temperature. This will not work if the player is offline or
     * they are currently dead.
     *
     * @param forceNew if true this update will instantly become the
     *                 players temperature.
     */
    public void updateTemperature(boolean forceNew) {
        if (!isTmpEnabled || tempScanner == null) return;
        Player player = Bukkit.getPlayer(id);
        if (player == null || !player.isOnline() || player.isDead()) return;

        WorldProvider provider = PLUGIN.getEngine().getWorldProvider();
        WorldData worldData = provider.getWorld(player.getWorld());
        World world = player.getWorld();

        // Stop if world is invalid.
        if (worldData == null) return;

        Location loc = player.getLocation();
        TemperatureProfile tempData = worldData.getTemperatures();

        // Handle temperature ticking.
        boolean inWater = isBlockWater(loc.getBlock());
        double emission = 0;

        // Add nearby block temperature if enabled.
        if (tmpUseBlocks) {
            if (forceNew) tempScanner.quickUpdate();
            else tempScanner.tick();
            emission += tempScanner.getTemperatureEmission();
        }

        // Add average nearby biome temperature if enabled
        if (tmpUseBiomes) {
            int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();
            emission += worldData.getBlockAmbientTemperature(x, y, z);
        }

        // Add environmental modifiers if enabled.
        if (tmpUseEnvironment) {
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
        if (tmpUseItems) {
            // Add armor to temp.
            ItemStack[] armor = player.getEquipment().getArmorContents();
            for (ItemStack i : armor) {
                if (i == null) continue;
                emission += tempData.getEmission(TempModifierType.CLOTHING, i.getType());
            }

            // Add temperature depending on what the player is holding
            Material mainHand = player.getInventory().getItemInMainHand().getType();
            Material offHand = player.getInventory().getItemInOffHand().getType();
            if (mainHand != Material.AIR) {
                double mainTemp = tempData.getEmission(TempModifierType.BLOCK, mainHand);
                emission += mainTemp / 10;
            }
            if (offHand != Material.AIR) {
                double offTemp = tempData.getEmission(TempModifierType.BLOCK, offHand);
                emission += offTemp / 10;
            }
        }

        if (tmpUseWeather) {
            emission += tempData.getEmission(TempModifierType.WEATHER, WeatherType.from(world));
        }

        if (!Double.isInfinite(emission) && !Double.isNaN(emission)) {
            this.tempExact = emission;
            tempData.updateMinMax(tempExact);
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
        if (!isTmpEnabled) return;

        // Change players temperature gradually
        double diff = MathUtil.diff(this.tempExact, this.temperature);
        if (diff > 0.09) {
            int division = 30;
            if (inWater && tempExact < temperature) {
                division = 10;
            }
            double delta = diff / division;
            if (delta > tmpMaxDelta) delta = tmpMaxDelta;
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
