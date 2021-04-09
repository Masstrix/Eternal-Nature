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

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Defines a range of options for debug information for the player to
 * turn off or on to aid in debugging issues.
 */
public class DebugOptions {

    public enum Type {
        DISABLE_AFK("disableAfk"),
        SHOW_SCAN_AREA("showScanArea"),
        LOG_OUTPUT("logOutput");

        private static final List<String> OPTIONS = new ArrayList<>();

        static {
            for (Type t : Type.values()) {
                OPTIONS.add(t.getSimpleName());
            }
            Collections.sort(OPTIONS);
        }

        public static List<String> getNames() {
            return OPTIONS;
        }

        private final boolean DEF;
        private final String NAME;

        Type(String name) {
            this(name, true);
        }

        Type(String name, boolean def) {
            DEF = def;
            this.NAME = name;
        }

        public String getSimpleName() {
            return NAME;
        }

        public boolean getDefault() {
            return DEF;
        }

        public void set(UserData data, boolean enabled) {
            data.getDebugOptions().set(this, enabled);
        }

        public boolean isEnabled(UserData data) {
            return data != null && data.getDebugOptions().isEnabled(this);
        }

        public static Type find(String name) {
            if (name == null || name.length() == 0) return null;
            for (Type t : Type.values()) {
                if (t.name().equalsIgnoreCase(name)
                        || t.getSimpleName().equalsIgnoreCase(name)) {
                    return t;
                }
            }
            return null;
        }
    }

    public Map<Type, Boolean> toggles = new HashMap<>();

    public void set(Type type, boolean enabled) {
        toggles.put(type, enabled);
    }

    public boolean isEnabled(Type type) {
        return toggles.getOrDefault(type, type.getDefault());
    }

    public void loadFromConfig(ConfigurationSection section) {
        toggles.clear();
        for (Type t : Type.values()) {
            boolean val = section.getBoolean(t.getSimpleName(), t.getDefault());
            if (val == t.getDefault()) continue;
            toggles.put(t, val);
        }
    }

    public void saveToConfig(ConfigurationSection section) {
        for (Map.Entry<Type, Boolean> toggle : toggles.entrySet()) {
            String key = toggle.getKey().getSimpleName();
            boolean val = toggle.getValue();
            section.set("debug-options." + key, val == toggle.getKey().getDefault() ? null : 0);
        }
    }
}
