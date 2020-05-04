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

package me.masstrix.version.checker;

import me.masstrix.version.MinecraftVersion;

public class VersionCheckInfo {

    private MinecraftVersion latest;
    private MinecraftVersion current;
    private VersionState state;

    public VersionCheckInfo(String current, String latest) {
        this.current = new MinecraftVersion(current);
        this.latest = new MinecraftVersion(latest);

        state = VersionState.get(this.current.compareTo(this.latest));
    }

    public MinecraftVersion getLatest() {
        return latest;
    }

    public MinecraftVersion getCurrent() {
        return current;
    }

    public VersionState getState() {
        return state;
    }

    public boolean isBehind() {
        return state == VersionState.BEHIND;
    }

    public boolean isLatest() {
        return state == VersionState.CURRENT;
    }

    public boolean isDev() {
        return state == VersionState.AHEAD;
    }

    public boolean isUnknown() {
        return state == VersionState.UNKNOWN;
    }
}
