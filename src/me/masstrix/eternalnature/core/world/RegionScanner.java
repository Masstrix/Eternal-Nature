/*
 * Copyright 2020 Matthew Denton
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
import me.masstrix.eternalnature.core.TemperatureData;
import me.masstrix.eternalnature.data.PlayerIdle;
import me.masstrix.eternalnature.data.UserData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class RegionScanner {

    private int iteration;
    private int fidelity;
    private int area, height;
    private long lastScanTime;
    private boolean done;
    private boolean setLoc;
    private final Player PLAYER;
    private final EternalNature PLUGIN;
    private TemperatureData tempData;
    private Location loc;
    private UserData user;

    private double temperature;
    private double hottest;
    private double coldest;

    public RegionScanner(EternalNature plugin, UserData data, Player player) {
        this.PLUGIN = plugin;
        this.PLAYER = player;
        this.user = data;
        this.tempData = plugin.getEngine().getTemperatureData();
        setFidelity(4);
        setScanScale(2, 2);
    }

    public void setFidelity(int fidelity) {
        this.fidelity = fidelity;
    }

    public void setScanScale(int area, int height) {
        this.area = area;
        this.height = height;
    }

    public double getTemperatureEmission() {
        return temperature;
    }

    public void stopScan() {
        this.done = true;
        this.iteration = 0;
    }

    public boolean isDoneScanning() {
        return done;
    }

    /**
     * Ticks the region scanner around the players location. The scanner will update it's
     * values once it's done enought iterations to match the fidelity.
     */
    public void tick() {
        if (PLAYER == null || !PLAYER.isOnline()) return;
        if (!setLoc) {
            done = false;
            setLoc = true;
            this.loc = PLAYER.getLocation().clone();
        }

        PlayerIdle idleInfo = user.getPlayerIdleInfo();

        // Reduce the number of calls being done when a player is in idle
        if (idleInfo.isAfk() && System.currentTimeMillis() - lastScanTime < 5000)
            return;

        // Reduce the scan area while a player is in idle/afk.
        int area = idleInfo.isDeepIdle() ? Math.max(this.area / 2, 2) : this.area;
        int fidelity = idleInfo.isDeepIdle() ? this.fidelity + 2 : this.fidelity;

        // Update the location on fast movement and reduce the area.
        Vector velocity = PLAYER.getVelocity();
        if (!idleInfo.isIdle() && velocity.length() > 1) {
            this.loc = PLAYER.getLocation().clone();
            area /= 1.3;
        }

        // center the scan area
        int areaHalf = area / 2;
        int heightHalf = this.height / 2;

        Block center = loc.getBlock();

        int areaSq = area * area; // squared area
        int areaCb = areaSq * this.height; // cubed area
        int scanSize = (areaCb / fidelity) + 1;

        for (int j = 0; j < scanSize; j++) {
            int i = j * fidelity + iteration;
            int sq = i % areaSq; // 2d squared area
            int x = sq % area - areaHalf;
            int z = sq / area - areaHalf;
            int y = (int) Math.floor((double) i / areaSq) - heightHalf; //i % areaSq; // depth

            Block block = center.getRelative(x, y, z);

            double temp = tempData.getBlockEmission(block.getType());
            double distance = block.getLocation().distance(loc);
            double fallOff = temp / distance;

            // Apply temperatures to hot and cold values
            if (i == 0) {
                hottest = fallOff;
                coldest = fallOff;
            }
            else if (fallOff > hottest) {
                hottest = fallOff;
            }
            else if (fallOff < coldest) {
                coldest = fallOff;
            }
        }

        iteration++;

        if (iteration >= fidelity) {
            iteration = 0;
            done = true;
            setLoc = false;

            //TODO use coldest
            this.temperature = hottest;
            lastScanTime = System.currentTimeMillis();
        }
    }
}
