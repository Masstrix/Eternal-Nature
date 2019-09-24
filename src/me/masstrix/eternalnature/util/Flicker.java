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

package me.masstrix.eternalnature.util;

/**
 * An auto switching toggle class updated everytime {@link #update()} is called.
 */
public class Flicker {

    private boolean enabled = true;
    private long lastSwitch = System.currentTimeMillis();
    private long delay;

    /**
     * @param delay how long between switches.
     */
    public Flicker(long delay) {
        this.delay = delay;
    }

    public boolean update() {
        long now = System.currentTimeMillis();
        if (now - lastSwitch >= delay) {
            lastSwitch = now;
            enabled = !enabled;
        }
        return enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
