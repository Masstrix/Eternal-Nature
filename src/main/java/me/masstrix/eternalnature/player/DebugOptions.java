/*
 * Copyright 2021 Matthew Denton
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

/**
 * Defines a range of options for debug information for the player to
 * turn off or on to aid in debugging issues.
 */
public class DebugOptions {

    private boolean disableAfk;
    private boolean showScanArea;

    public boolean isAfkDisabled() {
        return disableAfk;
    }

    public void setDisableAfk(boolean disableAfk) {
        this.disableAfk = disableAfk;
    }

    public boolean isShowScanArea() {
        return showScanArea;
    }

    public void setShowScanArea(boolean showScanArea) {
        this.showScanArea = showScanArea;
    }
}
