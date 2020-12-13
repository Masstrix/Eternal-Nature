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

package me.masstrix.eternalnature.core.temperature.maps;

import me.masstrix.eternalnature.config.Configurable;
import me.masstrix.eternalnature.core.temperature.TempModifierType;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.temperature.modifier.TemperatureModifier;
import me.masstrix.eternalnature.util.StringUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class TemperatureModifierMap<K> implements Configurable {

    protected final TemperatureProfile PROFILE;
    protected final String PATH;
    protected final Map<K, TemperatureModifier> MODIFIERS = new HashMap<>();
    private double min, max;

    public TemperatureModifierMap(TemperatureProfile profile, TempModifierType type) {
        this(profile, "data." + type.getConfigPath());
    }

    public TemperatureModifierMap(TemperatureProfile profile, String path) {
        this.PROFILE = profile;
        this.PATH = path;
    }

    @Override
    public final String getConfigPath() {
        return PATH;
    }

    @Override
    public abstract void updateConfig(ConfigurationSection section);

    /**
     * @return how many modifiers are in this map.
     */
    public int size() {
        return MODIFIERS.size();
    }

    /**
     * @return the hottest temperature indexed.
     */
    public double getMin() {
        return min;
    }

    /**
     * @return the colest temperature indexed.
     */
    public double getMax() {
        return max;
    }

    /**
     * Sets the modifier for a type.
     *
     * @param key key to set.
     * @param mod modifier to assign with that key.
     */
    public void setModifier(K key, TemperatureModifier mod) {
        MODIFIERS.put(key, mod);
    }

    /**
     * Gets a modifier of the generic type.
     *
     * @param key ket to get the modifier for.
     * @return the modifier for the given key or null it has none.
     */
    public TemperatureModifier getModifier(K key) {
        return MODIFIERS.get(key);
    }

    /**
     * An unsafe version of {@link #getModifier(Object)} where any object can be passed
     * in as a key.
     *
     * @param key ket to get the modifier for.
     * @return the modifier for the given key or null if it has none.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public TemperatureModifier getModifierUnsafe(Object key) {
        return MODIFIERS.get(key);
    }

    /**
     * Returns the emission value for a given unknown key type. This is an unsafe
     * call and will accept any object as input. If the input type is not of the
     * same type then 0 will be returned by default.
     *
     * @param key key to get emission value for.
     * @return the emission value for the key or 0 if null.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public double getEmissionUnsafe(Object key) {
        return MODIFIERS.getOrDefault(key, () -> 0).getEmission();
    }

    /**
     * Checks and returns if the object has a matching generic type. This will
     * return true if the object is an acceptable type for this mapping.
     *
     * @param obj object to check.
     * @return true if the object is the same or an accepted type for unsafe calls.
     */
    public boolean isGenericTypeSameAs(Object obj) {
        if (MODIFIERS.size() == 0) return false;
        Optional<K> type = MODIFIERS.keySet().stream().findAny();
        return obj.getClass().isAssignableFrom(type.get().getClass());
    }

    /**
     * Updates the min and max temperature value for this mapping.
     *
     * @param val value to update.
     */
    final void updateMinMax(double val) {
        if (val < min) min = val;
        if (val > max) max = val;
    }

    /**
     * Matches a name in a collection of keys. This is case insensitive and
     * will return the closest matching key from the collection.
     *
     * @see #findMatchingKey(String, Collection)
     *
     * @param matchTo string to match to.
     * @param section configuration section to look in.
     * @return the closest key in the confugration or null if no matching key was found.
     */
    final String findMatchingKey(String matchTo, ConfigurationSection section) {
        return findMatchingKey(matchTo, section.getKeys(false));
    }

    /**
     * Matches a name in a collection of keys. This is case insensitive and
     * will return the closest matching key from the collection.
     *
     * @param matchTo string to match to.
     * @param keys    collection of keys to look through. If a key
     *                ends eith {@code *} then it will use a closest
     *                match. If there is no star at the end then an
     *                exact match will be looked for only.
     * @return the closest key or null if no matching key was found.
     */
    final String findMatchingKey(String matchTo, Collection<String> keys) {
        if (matchTo == null) return null;
        String match = null;
        int diff = -1;

        // Search for the best matching biome setting in the config.
        for (final String KEY : keys) {
            // End search if an exact match is found
            if (KEY.equalsIgnoreCase(matchTo)) {
                return KEY;
            }

            // Use a closest match.
            if (KEY.endsWith("*")) {
                String mutated = KEY.toUpperCase();
                mutated = mutated.substring(0, KEY.length() - 1);
                if (!matchTo.contains(mutated)) continue;
                if (matchTo.equalsIgnoreCase(mutated)) {
                    match = KEY;
                    break;
                }
                int d = StringUtil.distanceContains(matchTo, mutated, true);
                if (diff == -1 || d < diff) {
                    diff = d;
                    match = KEY;
                }
            }
        }
        return match;
    }
}
