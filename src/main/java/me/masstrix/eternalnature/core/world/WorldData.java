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

package me.masstrix.eternalnature.core.world;

import me.masstrix.eternalnature.EternalNature;
import me.masstrix.eternalnature.api.EternalWorld;
import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.config.Configuration;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.world.wind.Wind;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class WorldData implements EternalWorld, Configurable {

    private final World WORLD;
    protected EternalNature plugin;
    private final TemperatureProfile TEMPERATURES;
    private final Wind WIND;

    public WorldData(EternalNature plugin, World world) {
        this.plugin = plugin;
        this.WORLD = world;
        this.WIND = new Wind(this, plugin, world.getSeed());

        // Load and set the temperature config.
        File cfg = new File(plugin.getDataFolder(), "/worlds/" + world + "/temperature-config.yml");
        TEMPERATURES = new TemperatureProfile(new Configuration(plugin, cfg)
                .setDefault(plugin.getEngine().getDefaultTempProfile().getConfig()));
        TEMPERATURES.name("World:" + this.WORLD);
        plugin.getRootConfig().subscribe(TEMPERATURES);

        plugin.getEngine().getWorldProvider().getHeartbeat().subscribe(WIND);
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        TEMPERATURES.reload();
        plugin.getRootConfig().reload(WIND);
    }

    /**
     * Unloads the world. This will make sure to unsubscribe any tasks that need to
     * be and save data that needs to be saved.
     */
    public void unload() {
        plugin.getEngine().getWorldProvider().getHeartbeat().unsubscribe(WIND);
        plugin.getRootConfig().unsubscribe(TEMPERATURES);
    }

    /**
     * @return the wind for this world.
     */
    public Wind getWind() {
        return WIND;
    }

    /**
     * Creates a new config file for the worlds temperature.
     */
    public boolean createCustomTemperatureConfig() {
        TEMPERATURES.getConfig().save();
        return true;
    }

    /**
     * @return the worlds name.
     */
    @Override
    public String getWorldName() {
        return WORLD.getName();
    }

    public World asBukkit() {
        return WORLD;
    }

    @Override
    public TemperatureProfile getTemperatures() {
        return TEMPERATURES;
    }

    /**
     * Returns the biome temperature for a block.
     *
     * @param x x block position.
     * @param y y block position.
     * @param z z block position.
     * @return the blocks biome temperature. Defaults to the defauly biome
     *         temperature set in the config.
     */
    public double getBiomeEmission(int x, int y, int z) {
        World world = asBukkit();
        if (world != null) {
            Biome biome = world.getBlockAt(x, y, z).getBiome();
            return TEMPERATURES.getBiome(biome, world);
        }
        return 0;
    }

    /**
     * Scans around in a circle
     *
     * @param points Number of points to sample in the ring.
     * @param rad    radius to scan from.
     * @param x      x center block position.
     * @param y      y center block position.
     * @param z      z center block position.
     * @return the ambient temperature of all the combined points for the given
     *         location.
     */
    public double getAmbientTemperature(int points, int rad, int x, int y, int z) {
        double total = getBiomeEmission(x, y, z);
        double increment = (2 * Math.PI) / points;

        for (int i = 0; i < points; i++) {
            double angle = i * increment;
            int blockX = (int) (x + (rad * Math.cos(angle)));
            int blockZ = (int) (z + (rad * Math.sin(angle)));
            total += getBiomeEmission(blockX, y, blockZ);
        }
        return total / (points + 1);
    }

    /**
     * Returns the current temperature of a block. This will vary depending on the biome,
     * sky light.
     *
     * @param x x block position.
     * @param y y block position.
     * @param z z block position.
     * @return the blocks temperature or <i>INFINITY</i> if there was an error.
     */
    public double getBlockAmbientTemperature(int x, int y, int z) {
        World world = asBukkit();
        if (world == null) return 0;
        Block block = world.getBlockAt(x, y, z);
        double temp = getAmbientTemperature(5, 15, x, y, z);

        // Apply modifier if block has sunlight.
        if (block.getLightFromSky() > 0) {
            double directSunAmplifier = TEMPERATURES.getDirectSunAmplifier() - 1;
            byte skyLight = block.getLightFromSky();
            double percent = skyLight / 15D;
            temp *= directSunAmplifier * percent + 1;
        }

        // Apply modifier if block is in a "cave"
        if (((block.getLightFromSky() <= 6 && block.getLightLevel() < 6)
                || block.getType() == Material.CAVE_AIR)
                && block.getLightLevel() != 15) {
            double amp = TEMPERATURES.getCaveModifier() - 1;
            byte light = block.getLightLevel();
            double percent = (15D - light) / 15D;
            temp *= amp * percent + 1;
        }
        return temp;
    }
}
