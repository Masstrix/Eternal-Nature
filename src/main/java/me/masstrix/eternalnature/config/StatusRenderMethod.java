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

package me.masstrix.eternalnature.config;

public enum StatusRenderMethod {
    ACTIONBAR("Actionbar", "Displays above the players hotbar.") {
        @Override
        public StatusRenderMethod next() {
            return XP_BAR;
        }
    },
    XP_BAR("XP Bar", "Uses the players experience bar.") {
        @Override
        public StatusRenderMethod next() {
            return BOSSBAR;
        }
    },
    BOSSBAR("Bossbar", "Shown at the top of the players screen.");

    private String simple;
    private String description;

    StatusRenderMethod(String simple, String description) {
        this.simple = simple;
        this.description = description;
    }

    public String getSimple() {
        return simple;
    }

    public String getDescription() {
        return description;
    }

    public StatusRenderMethod next() {
        return ACTIONBAR;
    }

    public StatusRenderMethod opposite() {
        return this == ACTIONBAR ? BOSSBAR : ACTIONBAR;
    }

    static StatusRenderMethod getOr(String val, StatusRenderMethod def) {
        for (StatusRenderMethod m : StatusRenderMethod.values()) {
            if (m.name().equalsIgnoreCase(val)) {
                return m;
            }
        }
        return def;
    }
}