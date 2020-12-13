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

import me.masstrix.eternalnature.core.temperature.TempModifierType;
import me.masstrix.eternalnature.core.temperature.TemperatureProfile;
import me.masstrix.eternalnature.core.temperature.modifier.BlockTemperature;
import me.masstrix.eternalnature.core.temperature.modifier.TemperatureModifier;
import me.masstrix.eternalnature.util.ConfigUtil;
import me.masstrix.eternalnature.util.Triplet;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public class BlockModifierMap extends TemperatureModifierMap<Material> {

    public BlockModifierMap(TemperatureProfile profile) {
        super(profile, TempModifierType.BLOCK);
    }

    @Override
    public void updateConfig(ConfigurationSection section) {
        for (Material m : Material.values()) {
            Triplet<BlockTemperature, Boolean, String> match = find(m, section);
            if (match == null || match.getFirst() == null) continue;

            setModifier(m, match.getFirst());
        }
    }

    public double getEmission(Block block) {
        return getEmission(block.getType());
    }

    public double getEmission(Material type) {
        BlockTemperature mod = getModifier(type);
        return mod == null ? 0 : mod.getEmission();
    }

    @Override
    public double getEmissionUnsafe(Object key) {
        if (key instanceof Material) {
            return super.MODIFIERS.getOrDefault(key, () -> 0).getEmission();
        }
        if (key instanceof Block) {
            return super.MODIFIERS.getOrDefault(((Block) key).getType(), () -> 0).getEmission();
        }
        return 0;
    }

    @Override
    public boolean isGenericTypeSameAs(Object obj) {
        return obj instanceof Material || obj instanceof Block;
    }

    public BlockTemperature getModifier(Block block) {
        return getModifier(block.getType());
    }

    @Override
    public BlockTemperature getModifier(Material material) {
        return (BlockTemperature) MODIFIERS.get(material);
    }

    @Override
    public TemperatureModifier getModifierUnsafe(Object key) {
        if (!isGenericTypeSameAs(key)) return null;
        if (key instanceof Block) return getModifier((Block) key);
        return getModifier((Material) key);
    }

    /**
     * Finds a match for the material in the config. If there is no match found then null be
     * returned.
     *
     * @param material material to fid a value for in the config.
     * @param section  section to look in in the config.
     * @return a triplet of a new BlockTemperature, if the data has nbt, and the matched key.
     */
    private Triplet<BlockTemperature, Boolean, String> find(Material material, ConfigurationSection section) {
        String materialName = material.name().toLowerCase();
        String matched = null;
        boolean nbt = false;

        for (String key : section.getKeys(false)) {
            if (key.equalsIgnoreCase(materialName)) {
                matched = key;
                break;
            }
            String type = key;
            boolean hasNbt = false;
            if (key.contains("[")) {
                hasNbt = true;
                type = key.substring(0, key.indexOf('['));
            }

            boolean endsWith = false;
            // Set to use starts with.
            if (type.endsWith("*")) {
                type.replace("*", "");
                endsWith = true;
            }

            String match = type.toLowerCase();

            if (endsWith ? match.startsWith(materialName) : match.equalsIgnoreCase(materialName)) {
                nbt = hasNbt;
                matched = key;
                break;
            }
        }

        if (matched == null) return null;
        ConfigUtil util = new ConfigUtil(section);
        BlockTemperature temp;
        if (util.isNumber(matched)) {
            temp = new BlockTemperature(util.getDouble(matched), PROFILE.getDefaultBlockScalar());
        } else {
            ConfigurationSection sec = section.getConfigurationSection(matched);
            if (sec == null) return null;
            double emission = sec.getDouble("emission");
            double scalar = sec.getDouble("falloff", PROFILE.getDefaultBlockScalar());
            temp = new BlockTemperature(emission, scalar);
        }

        return new Triplet<>(temp, material.isBlock() && nbt, matched);
    }
}
