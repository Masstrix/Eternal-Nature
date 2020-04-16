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

package me.masstrix.eternalnature.util;

import me.masstrix.eternalnature.EternalNature;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class BlockScanner {

    /**
     * Used for the position of a scanner.
     */
    public enum Position {
        CENTER, UP, DOWN
    }

    private int iteration;
    private int fidelity;
    private int area, height;
    private long lastScanTime;
    private boolean done;
    private boolean setLoc;
    private final EternalNature PLUGIN;
    private Location loc;
    private BlockScannerTask task;
    private Position yOffset = Position.CENTER;

    public BlockScanner(EternalNature plugin) {
        this.PLUGIN = plugin;
        setFidelity(2);
        setScanScale(2, 2);
    }

    public BlockScanner setTask(BlockScannerTask task) {
        this.task = task;
        return this;
    }

    public void setHeightOffset(Position position) {
        this.yOffset = position;
    }

    public void setFidelity(int fidelity) {
        this.fidelity = fidelity;
    }

    public void setScanScale(int area, int height) {
        this.area = area;
        this.height = height;
    }

    public boolean isDoneScanning() {
        return done;
    }

    /**
     * Sets the center point location where the scan is done.
     *
     * @param loc location of center point.
     */
    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public void scan() {
        this.scan(this.task);
    }

    /**
     * Ticks the region scanner around the players location. The scanner will update it's
     * values once it's done enought iterations to match the fidelity.
     */
    public void scan(BlockScannerTask task) {
        if (loc == null) return;
        if (!setLoc) {
            done = false;
            setLoc = true;
        }

        int area = this.area;
        int fidelity = this.fidelity;

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
            int y = 0;
            // Sets scan to be centered around location
            if (yOffset == Position.CENTER) {
                y = (int) Math.floor((double) i / areaSq) - heightHalf;
            }
            // Sets scan to be at and above location
            else if (yOffset == Position.UP) {
                y = (int) Math.floor((double) i / areaSq);
            }
            // Sets scan to be at and below location
            else if (yOffset == Position.DOWN) {
                y = (int) Math.floor((double) i / areaSq) - height;
            }

            Block block = center.getRelative(x, y, z);
            onBlockScanned(block);
            if (task != null) task.onBlockScan(block);
        }

        iteration++;

        if (iteration >= fidelity) {
            iteration = 0;
            done = true;
            lastScanTime = System.currentTimeMillis();
        }
    }

    private void onBlockScanned(Block block) {}
}
